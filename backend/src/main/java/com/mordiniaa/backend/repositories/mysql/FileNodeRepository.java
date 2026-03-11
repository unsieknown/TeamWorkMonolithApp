package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.models.file.cloudStorage.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FileNodeRepository extends JpaRepository<FileNode, UUID> {

    @Query("""
            select fn
            from FileNode fn
            where fn.deleted = false and fn.id = :id and fn.userStorage.userId = :userId and (fn.nodeType = 'DIRECTORY' or fn.nodeType = 'ROOT')
            """)
    Optional<FileNode> findDirByIdAndOwnerId(UUID id, UUID userId);

    @Query("""
            select fn
            from FileNode fn
            where fn.deleted = false and fn.id = :id and fn.userStorage.userId = :userId
            """)
    Optional<FileNode> findNodeByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select fn.id as id,
            fn.parentId as parentId,
            fn.storageKey storageKey,
            fn.nodeType as nodeType
            from FileNode fn
            where fn.id = :parentId and fn.userStorage.userId = :userId and fn.deleted = false
            """)
    Optional<FileNodeBaseMeta> findParentMetaProjection(UUID parentId, UUID userId);

    @Query("""
            select fn.id as id,
            fn.parentId as parentId,
            fn.nodeType as nodeType,
            fn.storageKey as storageKey
            from FileNode fn
            where fn.parentId = :currentId and fn.userStorage.userId = :userId
            """)
    List<FileNodeBaseMeta> findNodesByParentIdAndUserId(UUID currentId, UUID userId);

    @Modifying
    @Query("""
            update FileNode fn
            set fn.subTreeSize = fn.subTreeSize + :delta
            where fn.id in :dirIds and fn.userStorage.userId = :userId and fn.deleted = false
            """)
    void increaseTreeSize(Set<UUID> dirIds, UUID userId, long delta);

    @Modifying
    @Query("""
            update FileNode fn
            set fn.subTreeSize = fn.subTreeSize - :delta
            where fn.id in :dirIds and fn.userStorage.userId = :userId and fn.deleted = false
            """)
    void decreaseTreeSize(Set<UUID> dirIds, UUID userId, long delta);

    @Query("""
            select fn.id as id,
                   fn.storageKey as storageKey,
                   fn.nodeType as nodeType,
                   fn.userStorage.userId as userId
            from FileNode fn
            left join FileNode parent on fn.parentId = parent.id
            where fn.deleted = true and (fn.parentId is null or parent.deleted = false)
            """)
    List<FileNodeUserMeta> findFileNodesByDeletedTrue();

    List<FileNode> findFileNodesByParentIdAndUserStorage_UserIdAndDeletedFalse(UUID parentId, UUID userStorageUserId);

    @Query("""
            select fn
            from FileNode fn
            where fn.id in :ids and fn.userStorage.userId = :userId and fn.nodeType = 'DIRECTORY' and fn.deleted = false
            """)
    List<FileNodeBreadcrumb> getFileNodeBreadcrumbs(Set<UUID> ids, UUID userId);

    @Query("""
            select fn.id as id,
            fn.nodeType as nodeType,
            fn.name as name,
            fn.storageKey as storageKey
            from FileNode fn
            where fn.parentId = :nodeId and fn.deleted = false and fn.userStorage.userId = :userId and fn.nodeType != 'ROOT'
            """)
    List<FileNodeStorageKey> getSubNodeProjections(UUID nodeId, UUID userId);
}
