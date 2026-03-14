package com.mordiniaa.backend.models.file.cloudStorage;

import com.mordiniaa.backend.models.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "UserStorage")
@Table(name = "user_storage", indexes = @Index(name = "uq_user_storage_user_id", columnList = "user_id", unique = true))
public class UserStorage extends BaseEntity {

    @Version
    private long version;

    @Id
    @Column(name = "resource_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID resourceId;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "used_bytes", nullable = false)
    private Long usedBytes = 0L;

    @Column(name = "quota_bytes", nullable = false)
    private Long quotaBytes = 50_000_000_000L;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "root_node_id")
    private FileNode rootNode;

    @OneToMany(mappedBy = "userStorage", fetch = FetchType.LAZY)
    private List<FileNode> storedFiles = new ArrayList<>();

    public UserStorage(UUID userId) {
        this.userId = userId;
    }

    @PrePersist
    public void createRootIfNeeded() {
        if (rootNode == null) {
            FileNode root = new FileNode();
            root.setNodeType(NodeType.ROOT);
            root.setMaterializedPath(FileSystems.getDefault().getSeparator());
            root.setUserStorage(this);
            this.rootNode = root;
        }
    }
}
