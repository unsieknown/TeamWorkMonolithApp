package com.mordiniaa.backend.dto.file;

import com.mordiniaa.backend.models.file.cloudStorage.NodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "File Node Dto", description = "Schema To Hold Object Response Information")
public class FileNodeDto {

    @Schema(name = "id", description = "FileNode Id")
    private UUID id;

    @Schema(name = "parentPath", description = "Path Of The Parent Elemets")
    private String parentPath;

    @Schema(name = "name", description = "Resource Name")
    private String name;

    @Schema(name = "nodeType", description = "Node Type (FILE/DIR)")
    private String nodeType;

    @Schema(name = "size", description = "Resource Size In Bytes")
    private Long size;

    //Protection to not publish ROOT
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType.equals(NodeType.FILE) ? "FILE" : "DIRECTORY";
    }
}
