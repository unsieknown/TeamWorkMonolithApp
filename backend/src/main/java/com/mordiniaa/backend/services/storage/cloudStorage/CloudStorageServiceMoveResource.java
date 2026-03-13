package com.mordiniaa.backend.services.storage.cloudStorage;

import com.mordiniaa.backend.exceptions.BadRequestException;
import com.mordiniaa.backend.exceptions.FileNodeNotFound;
import com.mordiniaa.backend.models.file.cloudStorage.FileNode;
import com.mordiniaa.backend.models.file.cloudStorage.FileNodeBaseMeta;
import com.mordiniaa.backend.models.file.cloudStorage.NodeType;
import com.mordiniaa.backend.models.file.cloudStorage.UserStorage;
import com.mordiniaa.backend.repositories.mysql.FileNodeRepository;
import com.mordiniaa.backend.services.fileNode.FileNodeService;
import com.mordiniaa.backend.utils.CloudStorageServiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CloudStorageServiceMoveResource {

    private final FileNodeRepository fileNodeRepository;
    private final CloudStorageServiceUtils cloudStorageServiceUtils;

    @Transactional
    public void moveResourceUp(UUID from, UUID to, UUID userId) {

        if (Objects.equals(from, to))
            throw new BadRequestException("Paths Are The Same");

        FileNode source = fileNodeRepository.findNodeByIdAndUserId(from, userId)
                .orElseThrow(() -> new FileNodeNotFound("Requested Resource Not Found"));

        UserStorage storage = source.getUserStorage();
        FileNode target = cloudStorageServiceUtils.getParentNode(userId, to, storage);

        if (Objects.equals(source.getParentId(), target.getId()))
            throw new BadRequestException("Invalid Target");

        long sourceSize = source.getNodeType().equals(NodeType.DIRECTORY)
                ? source.getSubTreeSize()
                : source.getSize();

        List<FileNodeBaseMeta> parents = new ArrayList<>();
        FileNodeBaseMeta parentMeta = cloudStorageServiceUtils.getBaseFileProjection(source.getParentId(), userId);

        while (parentMeta != null && !Objects.equals(parentMeta.getId(), target.getId())) {
            parents.add(parentMeta);
            parentMeta = cloudStorageServiceUtils.getBaseFileProjection(parentMeta.getParentId(), userId);
        }

        if (parentMeta == null)
            throw new FileNodeNotFound("Requested Resource Not Found");

        Set<UUID> ids = new HashSet<>(parents.size());
        for (FileNodeBaseMeta p : parents) {
            ids.add(p.getId());
        }

        fileNodeRepository.decreaseTreeSize(ids, userId, sourceSize);

        source.setParentId(target.getId());
        source.setMaterializedPath(target);
        fileNodeRepository.save(source);
    }

    @Transactional
    public void moveResourceDown(UUID from, UUID to, UUID userId) {

        if (Objects.equals(from, to))
            throw new BadRequestException("Paths Are The Same");

        FileNode source = fileNodeRepository.findNodeByIdAndUserId(from, userId)
                .orElseThrow(() -> new FileNodeNotFound("Requested Resource Not Found"));

        FileNode target = fileNodeRepository.findDirByIdAndOwnerId(to, userId)
                .orElseThrow(() -> new FileNodeNotFound("Requested Resource Not Found"));

        if (!Objects.equals(source.getParentId(), target.getParentId()))
            throw new BadRequestException("Paths Are The Same");

        long sourceSize = source.getNodeType().equals(NodeType.DIRECTORY)
                ? source.getSubTreeSize()
                : source.getSize();

        source.setParentId(target.getId());
        source.setMaterializedPath(target);

        fileNodeRepository.increaseTreeSize(Set.of(target.getId()), userId, sourceSize);
        fileNodeRepository.save(source);
    }
}
