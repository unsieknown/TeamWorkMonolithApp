package com.mordiniaa.backend.services.storage.cloudStorage;

import com.mordiniaa.backend.config.StorageProperties;
import com.mordiniaa.backend.exceptions.BadRequestException;
import com.mordiniaa.backend.exceptions.StorageQuotaExceededException;
import com.mordiniaa.backend.exceptions.UnexpectedException;
import com.mordiniaa.backend.models.file.cloudStorage.FileNode;
import com.mordiniaa.backend.models.file.cloudStorage.NodeType;
import com.mordiniaa.backend.models.file.cloudStorage.UserStorage;
import com.mordiniaa.backend.repositories.mysql.FileNodeRepository;
import com.mordiniaa.backend.repositories.mysql.UserStorageRepository;
import com.mordiniaa.backend.services.fileNode.FileNodeService;
import com.mordiniaa.backend.services.storage.StorageProvider;
import com.mordiniaa.backend.utils.CloudStorageServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudStorageServiceCreateResource {

    private final StorageProvider storageProvider;
    private final FileNodeRepository fileNodeRepository;
    private final UserStorageRepository userStorageRepository;
    private final CloudStorageServiceUtils cloudStorageServiceUtils;
    private final StorageProperties storageProperties;

    @Transactional
    public void createDir(UUID userId, UUID parentId, String dirName) {

        if (cloudStorageServiceUtils.containsPathSeparator(dirName))
            throw new BadRequestException("Illegal Characters Found");

        UserStorage userStorage = cloudStorageServiceUtils.getOrCreateUserStorage(userId);

        FileNode parent = cloudStorageServiceUtils.getParentNode(userId, parentId, userStorage);
        FileNode dirNode = new FileNode(NodeType.DIRECTORY);
        if (parent != null) {
            dirNode.setParentId(parent.getId());
            dirNode.setMaterializedPath(parent);
        }

        dirNode.setName(dirName.trim());
        dirNode.setUserStorage(userStorage);
        dirNode.setStorageKey(null);
        fileNodeRepository.save(dirNode);
    }

    @Transactional
    public void uploadFile(UUID userId, UUID parentId, MultipartFile file) {

        if (file.isEmpty())
            throw new BadRequestException("Invalid File Sent");

        if (file.getOriginalFilename() == null || cloudStorageServiceUtils.containsPathSeparator(file.getOriginalFilename()))
            throw new BadRequestException("Metadata Mismatch");

        UserStorage userStorage = cloudStorageServiceUtils.getOrCreateUserStorage(userId);

        long fileSize = file.getSize();
        long usedSize = fileSize + userStorage.getUsedBytes();

        if (usedSize > userStorage.getQuotaBytes())
            throw new StorageQuotaExceededException("Storage quota exceeded");

        FileNode parent = cloudStorageServiceUtils.getParentNode(userId, parentId, userStorage);

        String storageKey = cloudStorageServiceUtils.buildStorageKey();

        try {
            storageProvider.upload(
                    storageProperties.getCloudStorage().getPath(),
                    storageKey,
                    file.getInputStream()
            );
        } catch (IOException ex) {
            throw new UnexpectedException("Unknown Error Occurred");
        }

        userStorage.setUsedBytes(usedSize);

        FileNode fileNode = new FileNode(NodeType.FILE);
        fileNode.setName(file.getOriginalFilename());
        fileNode.setUserStorage(userStorage);
        if (parent != null) {
            fileNode.setParentId(parent.getId());
            fileNode.setMaterializedPath(parent);
        }
        fileNode.setStorageKey(storageKey);
        fileNode.setSize(fileSize);

        Set<UUID> ids = cloudStorageServiceUtils.collectParentChain(parent, userId);

        try {
            fileNodeRepository.save(fileNode);
            userStorageRepository.save(userStorage);
            fileNodeRepository.increaseTreeSize(ids, userId, fileSize);
        } catch (Exception ex) {
            storageProvider.delete(
                    storageProperties.getCloudStorage().getPath(),
                    storageKey
            );
            throw ex;
        }
    }
}
