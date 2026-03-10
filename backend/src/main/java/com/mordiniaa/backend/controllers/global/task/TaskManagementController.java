package com.mordiniaa.backend.controllers.global.task;

import com.mordiniaa.backend.dto.task.TaskDetailsDTO;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.request.task.PatchTaskDataRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.task.TaskManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks/{taskId}/management")
public class TaskManagementController {

    private final AuthUtils authUtils;
    private final TaskManagementService taskManagementService;


    @PatchMapping
    public ResponseEntity<ApiResponse<TaskDetailsDTO>> updateTask(
            @PathVariable String taskId,
            @RequestParam("b") String boardId,
            @RequestBody PatchTaskDataRequest patchTaskDataRequest
    ) {

        UUID userId = authUtils.authenticatedUserId();
        TaskDetailsDTO dto = taskManagementService.updateTask(userId, boardId, taskId, patchTaskDataRequest);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Updated Successfully",
                        dto
                )
        );
    }
}
