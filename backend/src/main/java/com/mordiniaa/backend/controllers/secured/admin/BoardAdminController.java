package com.mordiniaa.backend.controllers.secured.admin;

import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.services.board.admin.BoardAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/board/{boardId}")
public class BoardAdminController {

    private final BoardAdminService boardAdminService;

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> setBoardOwner(
            @RequestParam(name = "u") UUID userId,
            @RequestParam(name = "t") UUID teamId,
            @PathVariable String boardId
    ) {

        boardAdminService.setBoardOwner(boardId, userId, teamId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Updated Successfully",
                        null
                )
        );
    }
}
