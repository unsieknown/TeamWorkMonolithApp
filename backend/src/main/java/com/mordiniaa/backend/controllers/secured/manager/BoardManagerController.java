package com.mordiniaa.backend.controllers.secured.manager;

import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.payload.APIResponse;
import com.mordiniaa.backend.request.board.BoardCreationRequest;
import com.mordiniaa.backend.request.board.PermissionsRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.board.owner.BoardOwnerManagementService;
import com.mordiniaa.backend.services.board.owner.BoardOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager/board")
public class BoardManagerController {

    private final BoardOwnerService boardOwnerService;
    private final AuthUtils authUtils;
    private final BoardOwnerManagementService boardOwnerManagementService;

    @PostMapping
    public ResponseEntity<APIResponse<BoardDetailsDto>> createBoard(@Valid @RequestBody BoardCreationRequest boardCreationRequest) {
        UUID managerId = authUtils.authenticatedUserId();
        BoardDetailsDto dto = boardOwnerService.createBoard(managerId, boardCreationRequest);

        return ResponseEntity.ok(
                new APIResponse<>(
                        "Created Successfully",
                        dto
                )
        );
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<APIResponse<Void>> changeBoardMemberPermissions(
            @PathVariable String boardId,
            @RequestParam("u") UUID userId,
            @Valid @RequestBody PermissionsRequest permissionsRequest
    ) {
        UUID managerId = authUtils.authenticatedUserId();
        boardOwnerManagementService.changeBoardMemberPermissions(managerId, boardId, userId, permissionsRequest);

        return ResponseEntity.ok(
                new APIResponse<>(
                        "Updated",
                        null
                )
        );
    }

    @PutMapping("/{boardId}/archive")
    public ResponseEntity<APIResponse<Void>> archiveBoard(
            @PathVariable String boardId
    ) {

        UUID managerId = authUtils.authenticatedUserId();
        boardOwnerManagementService.archiveBoard(managerId, boardId);
        return ResponseEntity.ok(
                new APIResponse<>(
                        "Archived Successfully",
                        null
                )
        );
    }

    @PutMapping("/{boardId}/restore")
    public ResponseEntity<APIResponse<Void>> restoreBoard(
            @PathVariable String boardId
    ) {

        UUID managerId = authUtils.authenticatedUserId();
        boardOwnerManagementService.restoreBoard(managerId, boardId);
        return ResponseEntity.ok(
                new APIResponse<>(
                        "Restored Successfully",
                        null
                )
        );
    }

    @PutMapping("/{boardId}/user/{userId}")
    public void addUserToBoard(
            @PathVariable String boardId,
            @PathVariable UUID userId,
            @RequestParam("op") String op
            ) {

        System.err.println("DUPA");
        UUID managerId = authUtils.authenticatedUserId();
        String operation = op.toLowerCase();
        switch (operation) {
            case "add" -> boardOwnerService.addUserToBoard(managerId, userId, boardId);
            case "remove" -> boardOwnerService.removeUserFromBoard(managerId, userId, boardId);
            default -> throw new RuntimeException("Unsupported Operation");
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<APIResponse<Void>> deleteBoard(@PathVariable String boardId) {

        UUID managerId = authUtils.authenticatedUserId();
        boardOwnerService.deleteBoard(managerId, boardId);

        return ResponseEntity.ok(
                new APIResponse<>(
                        "Successfully Deleted",
                        null
                )
        );
    }
}
