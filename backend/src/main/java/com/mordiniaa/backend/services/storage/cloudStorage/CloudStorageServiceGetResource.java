package com.mordiniaa.backend.services.storage.cloudStorage;

import com.mordiniaa.backend.config.StorageProperties;
import com.mordiniaa.backend.dto.file.FileNodeDto;
import com.mordiniaa.backend.mappers.file.FIleNodeMapper;
import com.mordiniaa.backend.models.file.cloudStorage.*;
import com.mordiniaa.backend.repositories.mysql.FileNodeRepository;
import com.mordiniaa.backend.services.storage.StorageProvider;
import com.mordiniaa.backend.utils.CloudStorageServiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloudStorageServiceGetResource {

    private final FileNodeRepository fileNodeRepository;
    private final CloudStorageServiceUtils cloudStorageServiceUtils;
    private final FIleNodeMapper fileNodeMapper;
    private final StorageProvider storageProvider;
    private final StorageProperties storageProperties;

    @Transactional
    public List<FileNodeDto> getResourceListRootLvl(UUID userId) {

        UserStorage userStorage = cloudStorageServiceUtils.getOrCreateUserStorage(userId);
        FileNode rootNode = userStorage.getRootNode();

        if (rootNode == null) {
            return List.of();
        }

        return fileNodeRepository.findFileNodesByParentIdAndUserStorage_UserIdAndDeletedFalse(rootNode.getId(), userId)
                .stream()
                .map(node -> fileNodeMapper.toDto(node, "/"))
                .toList();
    }

    @Transactional
    public List<FileNodeDto> getResourceList(UUID userId, UUID dirId) {

        FileNode requestedDir = fileNodeRepository.findDirByIdAndOwnerId(dirId, userId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        List<UUID> ids = Arrays.stream(requestedDir.getMaterializedPath().split("/"))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .toList();


        Map<UUID, FileNodeBreadcrumb> breadcrumbs = fileNodeRepository.getFileNodeBreadcrumbs(new HashSet<>(ids), userId)
                .stream()
                .collect(Collectors.toMap(
                        FileNodeBreadcrumb::getId,
                        Function.identity()
                ));

        StringBuilder sb = new StringBuilder();
        for (UUID id : ids) {
            FileNodeBreadcrumb node = breadcrumbs.get(id);
            if (node != null)
                sb.append("/").append(node.getName());
        }

        String path = sb.toString();
        return fileNodeRepository.findFileNodesByParentIdAndUserStorage_UserIdAndDeletedFalse(requestedDir.getId(), userId)
                .stream()
                .map(node -> fileNodeMapper.toDto(node, path))
                .toList();
    }

    public ResponseEntity<StreamingResponseBody> downloadResource(UUID userId, UUID resourceId) {

        FileNode node = fileNodeRepository.findNodeByIdAndUserId(resourceId, userId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        if (node.getNodeType().equals(NodeType.ROOT))
            throw new RuntimeException();

        StreamingResponseBody streamingResponseBody;
        if (node.getNodeType().equals(NodeType.FILE))
            streamingResponseBody = handleFileDownload(node);
        else
            streamingResponseBody = handleDirDownload(node, userId);

        String fileName = node.getNodeType().equals(NodeType.FILE)
                ? node.getName()
                : node.getName() + ".zip";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(streamingResponseBody);
    }

    private StreamingResponseBody handleFileDownload(FileNode fileNode) {
        String storageKey = fileNode.getStorageKey();
        return outputStream -> {
            try (InputStream in = storageProvider.downloadFile(
                    storageProperties.getCloudStorage().getPath(),
                    storageKey
            )) {
                in.transferTo(outputStream);
            }
        };
    }

    private StreamingResponseBody handleDirDownload(FileNode fileNode, UUID userId) {

        Map<String, Object> tree = new HashMap<>();

        fillTree(fileNode.getId(), tree, userId);

        Map.Entry<String, Map<String, Object>> treeRoot = new AbstractMap.SimpleEntry<>(
                fileNode.getName(),
                tree
        );

        return storageProvider.downloadDir(
                storageProperties.getCloudStorage().getPath(),
                treeRoot
        );
    }

    private void fillTree(UUID branchId, Map<String, Object> tree, UUID userId) {

        List<FileNodeStorageKey> subNodes = fileNodeRepository.getSubNodeProjections(branchId, userId);
        for (FileNodeStorageKey node : subNodes) {
            if (node.getNodeType().equals(NodeType.FILE)) {
                tree.put(node.getName(), node);
            } else {
                Map<String, Object> subTree = new HashMap<>();
                fillTree(node.getId(), subTree, userId);
                tree.put(node.getName(), subTree);
            }
        }
    }
}
