package com.mordiniaa.backend.controllers.global;

import com.mordiniaa.backend.dto.task.TaskDetailsDTO;
import com.mordiniaa.backend.dto.task.TaskShortDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.request.task.CreateTaskRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.task.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final AuthUtils authUtils;
    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDetailsDTO>> getTaskDetails(
            @RequestParam("b") String boardId,
            @PathVariable String taskId
    ) {
        UUID userId = authUtils.authenticatedUserId();
        TaskDetailsDTO dto = taskService.getTaskDetailsById(userId, boardId, taskId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Successfully Created",
                        dto
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskShortDto>> createTask(
            @RequestParam("b") String boardId,
            @RequestParam("cn") String categoryName,
            @Valid @RequestBody CreateTaskRequest createTaskRequest
    ) {

        UUID userId = authUtils.authenticatedUserId();
        TaskShortDto dto = taskService.createTask(userId, boardId, categoryName, createTaskRequest);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Successfully Created",
                        dto
                )
        );
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTaskById(
            @RequestParam("b") String boardId,
            @PathVariable String taskId
    ) {
        UUID userId = authUtils.authenticatedUserId();
        taskService.deleteTaskFromBoard(userId, boardId, taskId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Successfully Created",
                        null
                )
        );
    }
}
