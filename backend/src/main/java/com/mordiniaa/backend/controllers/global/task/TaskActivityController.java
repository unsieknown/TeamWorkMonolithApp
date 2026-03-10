package com.mordiniaa.backend.controllers.global.task;

import com.mordiniaa.backend.dto.task.TaskDetailsDTO;
import com.mordiniaa.backend.dto.task.TaskShortDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.request.task.UpdateTaskPositionRequest;
import com.mordiniaa.backend.request.task.UploadCommentRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.task.TaskActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v/tasks/{taskId}/activity")
public class TaskActivityController {

    private final AuthUtils authUtils;
    private final TaskActivityService taskActivityService;

    @PutMapping("/position")
    public ResponseEntity<ApiResponse<TaskShortDto>> changeTaskPosition(
            @PathVariable String taskId,
            @RequestParam("b") String boardId,
            @Valid @RequestBody UpdateTaskPositionRequest updateTaskPositionRequest
            ) {

        UUID userId = authUtils.authenticatedUserId();

        TaskShortDto dto = taskActivityService.changeTaskPosition(userId, boardId, taskId, updateTaskPositionRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Updated Successfully",
                        dto
                )
        );
    }

    @PostMapping("/comment")
    public ResponseEntity<ApiResponse<TaskDetailsDTO>> writeComment(
            @PathVariable String taskId,
            @RequestParam("b") String boardId,
            @Valid @RequestBody UploadCommentRequest updateTaskPositionRequest
    ) {

        UUID userId = authUtils.authenticatedUserId();

        TaskDetailsDTO dto = taskActivityService.writeComment(userId, boardId, taskId, updateTaskPositionRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Commented Successfully",
                        dto
                )
        );
    }

    @PutMapping("/comment")
    public ResponseEntity<ApiResponse<TaskDetailsDTO>> updateComment(
            @PathVariable String taskId,
            @RequestParam("b") String boardId,
            @Valid @RequestBody UploadCommentRequest updateTaskPositionRequest
    ) {

        UUID userId = authUtils.authenticatedUserId();

        TaskDetailsDTO dto = taskActivityService.updateComment(userId, boardId, taskId, updateTaskPositionRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Commented Successfully",
                        dto
                )
        );
    }
}
