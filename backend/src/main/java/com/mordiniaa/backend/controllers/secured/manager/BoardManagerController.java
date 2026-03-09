package com.mordiniaa.backend.controllers.secured.manager;

import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.request.board.BoardCreationRequest;
import com.mordiniaa.backend.request.board.PermissionsRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
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

    @PostMapping
    public ResponseEntity<ApiResponse<BoardDetailsDto>> createBoard(@Valid @RequestBody BoardCreationRequest boardCreationRequest) {
        UUID managerId = authUtils.authenticatedUserId();
        BoardDetailsDto dto = boardOwnerService.createBoard(managerId, boardCreationRequest);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Created Successfully",
                        dto
                )
        );
    }

    public void changeBoardMemberPermissions(@Valid @RequestBody PermissionsRequest permissionsRequest) {

    }

    @PutMapping("/archive")
    public void archiveBoard(
            @RequestParam(name = "u") UUID ownerId,
            @RequestParam(name = "b") String boardId
    ) {

    }

    @PutMapping("/restore")
    public void restoreBoard(
            @RequestParam(name = "u") UUID ownerId,
            @RequestParam(name = "b") String boardId
    ) {

    }

    @PutMapping("/user/{operation}")
    public void addUserToBoard(
            @PathVariable String operation,
            @RequestParam(name = "u") UUID userId,
            @RequestParam(name = "b") String boardId
    ) {
        if (operation.equals("add")) {
            boardOwnerService.addUserToBoard(UUID.randomUUID(), userId, boardId);
        } else if (operation.equals("remove")) {
            boardOwnerService.removeUserFromBoard(UUID.randomUUID(), userId, boardId);
        }
    }

    public void deleteBoard(@RequestParam(name = "b") String boardId) {

    }
}
