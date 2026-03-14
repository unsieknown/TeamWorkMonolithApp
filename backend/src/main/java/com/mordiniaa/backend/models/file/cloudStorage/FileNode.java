package com.mordiniaa.backend.models.file.cloudStorage;

import com.mordiniaa.backend.models.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "FileNode")
@Table(name = "file_nodes")
public class FileNode extends BaseEntity {

    @Version
    @Column(name = "version")
    private Long version;

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "name")
    private String name;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "materialized_path")
    private String materializedPath;

    // For Files
    @Column(name = "size")
    private Long size = 0L;

    // For Dirs
    @Column(name = "sub_tree_size")
    private Long subTreeSize = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_storage_id", referencedColumnName = "resource_id")
    private UserStorage userStorage;

    public FileNode(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public void setMaterializedPath(String path) {
        this.materializedPath = path;
    }

    public void setMaterializedPath(FileNode parent) {
        String parentPath = parent.getMaterializedPath();
        if (parentPath == null) {
            throw new RuntimeException();
        }

        if (parent.getNodeType().equals(NodeType.ROOT))
            this.materializedPath = parentPath + this.id;
        else
            this.materializedPath = parentPath + FileSystems.getDefault().getSeparator() + this.id;
    }
}