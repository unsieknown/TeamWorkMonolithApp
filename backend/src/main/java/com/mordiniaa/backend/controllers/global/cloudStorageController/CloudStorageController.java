package com.mordiniaa.backend.controllers.global.cloudStorageController;

import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.storage.cloudStorage.CloudStorageServiceCreateResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage/resource")
public class CloudStorageController {

    private final AuthUtils authUtils;
    private final CloudStorageServiceCreateResource cloudStorageServiceCreateResource;

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
}
