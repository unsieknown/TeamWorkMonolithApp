package com.mordiniaa.backend.controllers.secured.manager;

import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.request.board.TaskCategoryRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.board.owner.BoardOwnerManagementService;
import com.mordiniaa.backend.services.board.owner.BoardOwnerTaskCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager/board/{boardId}/category")
public class BoardTaskCategoryManagerController {


    private final BoardOwnerTaskCategoryService boardOwnerTaskCategoryService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<BoardDetailsDto>> createTaskCategory(
            @PathVariable String boardId,
            @RequestBody TaskCategoryRequest taskCategoryRequest
    ) {

        UUID managerId = authUtils.authenticatedUserId();
        BoardDetailsDto dto = boardOwnerTaskCategoryService.createTaskCategory(managerId, boardId, taskCategoryRequest);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        "Created Successfully",
                        dto
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/rename")
    public ResponseEntity<ApiResponse<BoardDetailsDto>> renameTaskCategory(
        @PathVariable String boardId,
        @RequestParam(name = "t") UUID teamId,
        @RequestBody TaskCategoryRequest taskCategoryRequest
    ) {

        UUID managerId = authUtils.authenticatedUserId();
        BoardDetailsDto dto = boardOwnerTaskCategoryService.renameTaskCategory(managerId, boardId, teamId, taskCategoryRequest);
        return new ResponseEntity<>(
                new ApiResponse<>(
                        "Created Successfully",
                        dto
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/reorder")
    public void reorderTaskCategories(
            @PathVariable String boardId,
            @RequestParam(name = "t") UUID teamId,
            @RequestParam(name = "p") Integer newPosition,
            @RequestBody TaskCategoryRequest taskCategoryRequest
    ) {

    }

    @DeleteMapping
    public void deleteTaskCategory(
            @PathVariable String boardId,
            @RequestParam(name = "t") UUID teamId,
            @RequestBody TaskCategoryRequest taskCategoryRequest
    ) {

    }
}
