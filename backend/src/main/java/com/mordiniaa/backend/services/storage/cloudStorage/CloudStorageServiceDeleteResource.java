package com.mordiniaa.backend.services.storage.cloudStorage;

import com.mordiniaa.backend.config.StorageProperties;
import com.mordiniaa.backend.exceptions.FileNodeNotFound;
import com.mordiniaa.backend.models.file.cloudStorage.*;
import com.mordiniaa.backend.repositories.mysql.FileNodeRepository;
import com.mordiniaa.backend.repositories.mysql.UserStorageRepository;
import com.mordiniaa.backend.services.storage.StorageProvider;
import com.mordiniaa.backend.utils.CloudStorageServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudStorageServiceDeleteResource {

    private final FileNodeRepository fileNodeRepository;
    private final StorageProvider storageProvider;
    private final UserStorageRepository userStorageRepository;
    private final CloudStorageServiceUtils cloudStorageServiceUtils;
    private final StorageProperties storageProperties;

    @Transactional
    public void deleteFileNode(UUID userId, UUID nodeId) {

        FileNode resource = fileNodeRepository
                .findNodeByIdAndUserId(nodeId, userId)
                .orElseThrow(() -> new FileNodeNotFound("Requested Resource Not Found"));

        UserStorage userStorage = resource.getUserStorage();
        long resourceSize = resource.getNodeType().equals(NodeType.DIRECTORY)
                ? resource.getSubTreeSize()
                : resource.getSize();
        long newUsedValue = userStorage.getUsedBytes() - resourceSize;

        resource.setDeleted(true);
        userStorage.setUsedBytes(Math.max(newUsedValue, 0));

        fileNodeRepository.save(resource);
        userStorageRepository.save(userStorage);

        FileNode resourceParent = resource.getParentId() == null
                ? null
                : fileNodeRepository.findNodeByIdAndUserId(resource.getParentId(), userId).orElse(null);

        if (resourceParent != null) {
            Set<UUID> ids = cloudStorageServiceUtils.collectParentChain(resourceParent, userId);
            fileNodeRepository.decreaseTreeSize(ids, userId, resourceSize);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 */5 * * *")
    public void fullDelete() {

        List<FileNodeUserMeta> deletedNodes = fileNodeRepository
                .findFileNodesByDeletedTrue();

        log.info("Deleting {} files", deletedNodes.size());

        List<String> keysToDelete = new ArrayList<>();
        Set<UUID> idsToDelete = new HashSet<>();

        for (FileNodeUserMeta meta : deletedNodes) {
            idsToDelete.add(meta.getId());
            if (!meta.getNodeType().equals(NodeType.DIRECTORY) && meta.getStorageKey() != null) {
                log.info("Deleting file: {}, id: {}", meta.getStorageKey(), meta.getId());
                keysToDelete.add(meta.getStorageKey());
                continue;
            }

            Queue<UUID> idsQueue = new ArrayDeque<>();
            idsQueue.add(meta.getId());

            UUID userId = meta.getUserId();
            while (!idsQueue.isEmpty()) {

                UUID currentId = idsQueue.poll();
                List<FileNodeBaseMeta> childrenMeta = fileNodeRepository.findNodesByParentIdAndUserId(currentId, userId);
                for (FileNodeBaseMeta node : childrenMeta) {
                    idsToDelete.add(node.getId());
                    if (!node.getNodeType().equals(NodeType.DIRECTORY)) {
                        keysToDelete.add(node.getStorageKey());
                    } else {
                        idsQueue.add(node.getId());
                    }
                }
            }
        }

        log.info("Keys to Delete: {}", keysToDelete);
        keysToDelete.forEach(key -> storageProvider.delete(
                storageProperties.getCloudStorage().getPath(),
                key
        ));
        fileNodeRepository.deleteAllById(idsToDelete);
    }
}
