package com.mordiniaa.backend.controllers.global.cloudStorageController;

import com.mordiniaa.backend.dto.file.FileNodeDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.payload.PageMeta;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceCreateResource;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceGetResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage/resource")
public class CloudStorageController {

    private final AuthUtils authUtils;
    private final CloudStorageServiceCreateResource cloudStorageServiceCreateResource;
    private final CloudStorageServiceGetResource cloudStorageServiceGetResource;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> upload(
            @RequestBody MultipartFile file,
            @RequestParam(value = "parentId", required = false) UUID parentId
            ) {

        UUID userId = authUtils.authenticatedUserId();
        cloudStorageServiceCreateResource.uploadFile(userId, parentId, file);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        "Uploaded Successfully",
                        null
                ),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createDir(
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @RequestParam("dirName") String dirName
    ) {

        UUID userId = authUtils.authenticatedUserId();
        cloudStorageServiceCreateResource.createDir(userId, parentId, dirName);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        "Directory Created",
                        null
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public ResponseEntity<CollectionResponse<FileNodeDto>> getResourceList(
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
                new CollectionResponse<>(
                        nodes,
                        pageMeta
                )
        );
    }

    @GetMapping("/details/{nodeId}")
    public void getResourceDetails(
            @RequestParam("node") UUID nodeId
    ) {

    }

    @GetMapping("/download/{nodeId}")
    public void downloadResource() {

    }

    @PutMapping("/move")
    public void moveResource(
            @RequestParam("from") UUID from,
            @RequestParam("to") UUID to,
            @RequestParam("direction") String direction
    ) {

    }

    @DeleteMapping("/{nodeId}")
    public void deleteResource() {

    }
}
