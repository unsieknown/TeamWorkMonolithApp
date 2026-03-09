package com.mordiniaa.backend.controllers.global;

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
}
