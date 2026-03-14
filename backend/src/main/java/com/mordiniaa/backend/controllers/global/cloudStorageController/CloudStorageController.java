package com.mordiniaa.backend.controllers.global.cloudStorageController;

import com.mordiniaa.backend.dto.file.FileNodeDto;
import com.mordiniaa.backend.exceptions.UnsupportedOperationException;
import com.mordiniaa.backend.payload.APIResponse;
import com.mordiniaa.backend.payload.PageMeta;
import com.mordiniaa.backend.payload.nodeDto.CollectionNodeDtoResponse;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceCreateResource;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceDeleteResource;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceGetResource;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceMoveResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Cloud Storage Controller", description = "Controller For Managing Cloud storage. Required Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage/resource")
public class CloudStorageController {

    private final AuthUtils authUtils;
    private final CloudStorageServiceCreateResource cloudStorageServiceCreateResource;
    private final CloudStorageServiceGetResource cloudStorageServiceGetResource;
    private final CloudStorageServiceMoveResource cloudStorageServiceMoveResource;
    private final CloudStorageServiceDeleteResource cloudStorageServiceDeleteResource;

    @Operation(
            summary = "Upload New File",
            description = "User Can Upload File To Specified Node Defined By Parent Id Representing Directory"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "File Uploaded Successfully. Resource Created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid File or Metadata"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource Not Found"
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "Storage Limit Reached"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unknow Error On Server Side"
            )
    })
    @PostMapping("/upload")
    public ResponseEntity<APIResponse<Void>> upload(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "File To Upload", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            @RequestBody MultipartFile file,

            @Parameter(in = ParameterIn.QUERY, name = "parentId", description = "Id Of The Parent Node To Chain Elements", content = @Content(schema = @Schema(implementation = UUID.class)))
            @RequestParam(name = "parentId", required = false) UUID parentId
    ) {

        UUID userId = authUtils.authenticatedUserId();
        cloudStorageServiceCreateResource.uploadFile(userId, parentId, file);

        return new ResponseEntity<>(
                new APIResponse<>(
                        "Uploaded Successfully",
                        null
                ),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Create Directory",
            description = "Creates Virtual Dir In File Node Format For Chaining Nodes In Structure Requested By User"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "File Node Created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid Data Provided"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parent Node Not Found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected Error On Server Side"
            )
    })
    @PostMapping("/create")
    public ResponseEntity<APIResponse<Void>> createDir(
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @RequestParam("dirName") String dirName
    ) {

        UUID userId = authUtils.authenticatedUserId();
        cloudStorageServiceCreateResource.createDir(userId, parentId, dirName);

        return new ResponseEntity<>(
                new APIResponse<>(
                        "Directory Created",
                        null
                ),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "List Current Directory",
            description = "Returns JSON with files metadata in current directory"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List Returned",
                    content = @Content(
                            schema = @Schema(implementation = CollectionNodeDtoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            )
    })
    @GetMapping("/list")
    public ResponseEntity<CollectionNodeDtoResponse> getResourceList(
            @RequestParam(value = "node", required = false) UUID nodeId
    ) {

        UUID userId = authUtils.authenticatedUserId();

        List<FileNodeDto> nodes;
        if (nodeId == null) {
            nodes = cloudStorageServiceGetResource.getResourceListRootLvl(userId);
        } else {
            nodes = cloudStorageServiceGetResource.getResourceList(userId, nodeId);
        }

        PageMeta pageMeta = new PageMeta();
        pageMeta.setTotalPages(1);
        pageMeta.setPage(0);
        pageMeta.setSize(nodes.size());
        pageMeta.setLastPage(true);
        pageMeta.setTotalItems(nodes.size());

        return ResponseEntity.ok(
                new CollectionNodeDtoResponse(
                        nodes,
                        pageMeta
                )
        );
    }

    @Operation(
            summary = "Download",
            description = "Download Specified File"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File Downloaded"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Unsupported Operation"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource Not Found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unknown Exception"
            )
    })
    @GetMapping("/download/{nodeId}")
    public ResponseEntity<StreamingResponseBody> downloadResource(
            @Parameter(in = ParameterIn.PATH, required = true, description = "Id Of The Node", content = @Content(schema = @Schema(implementation = UUID.class)))
            @PathVariable UUID nodeId
    ) {

        UUID userId = authUtils.authenticatedUserId();
        return cloudStorageServiceGetResource.downloadResource(userId, nodeId);
    }

    @Operation(
            summary = "Move Resource",
            description = "Move Resource In Other Place"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource Successfully Moved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid Data or Unsupported Operation"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource Not Found"
            )
    })
    @PutMapping("/move")
    public ResponseEntity<APIResponse<Void>> moveResource(
            @Parameter(in = ParameterIn.QUERY, description = "Source", content = @Content(schema = @Schema(implementation = UUID.class)))
            @RequestParam(value = "from", required = false) UUID from,

            @Parameter(in = ParameterIn.QUERY, description = "Target", content = @Content(schema = @Schema(implementation = UUID.class)))
            @RequestParam(value = "to", required = false) UUID to,

            @Parameter(in = ParameterIn.QUERY, description = "Direction", required = true, content = @Content(schema = @Schema(implementation = String.class)))
            @RequestParam("direction") String direction
    ) {

        UUID userId = authUtils.authenticatedUserId();
        if (direction.equals("down"))
            cloudStorageServiceMoveResource.moveResourceDown(from, to, userId);
        else if (direction.equals("up"))
            cloudStorageServiceMoveResource.moveResourceUp(from, to, userId);
        else throw new UnsupportedOperationException("Unsupported Operation");

        return ResponseEntity.ok(
                new APIResponse<>(
                        "Resource Moved Successfully",
                        null
                )
        );
    }

    @Operation(
            summary = "Delete File Or Directory",
            description = "Deletes Specified File Node. File or Directory With Whole Tree Under It"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully Deleted"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File Node Not Found"
            )
    })
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<Void> deleteResource(
            @Parameter(in = ParameterIn.PATH, required = true, description = "Id Of Specified Node", content = @Content(schema = @Schema(implementation = UUID.class)))
            @PathVariable UUID nodeId
    ) {
        UUID userId = authUtils.authenticatedUserId();
        cloudStorageServiceDeleteResource.deleteFileNode(userId, nodeId);

        return ResponseEntity.noContent().build();
    }
}
