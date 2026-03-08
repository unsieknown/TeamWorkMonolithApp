package com.mordiniaa.backend.controllers.secured.admin;

import com.mordiniaa.backend.dto.team.TeamShortDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.request.team.TeamCreationRequest;
import com.mordiniaa.backend.services.team.TeamAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/team")
public class TeamAdminController {

    private final TeamAdminService teamAdminService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamShortDto>> createTeam(@Valid @RequestBody TeamCreationRequest teamCreationRequest) {

        TeamShortDto dto = teamAdminService.createTeam(teamCreationRequest);
        ApiResponse<TeamShortDto> response = new ApiResponse<>(
                "Success",
                dto
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{teamId}/manager/{managerId}")
    public ResponseEntity<ApiResponse<TeamShortDto>> assignManagerToTeam(
            @PathVariable UUID managerId,
            @PathVariable UUID teamId
    ) {
        TeamShortDto dto = teamAdminService.assignManagerToTeam(managerId, teamId);
        ApiResponse<TeamShortDto> response = new ApiResponse<>(
                "Assigned",
                dto
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{teamId}/manager")
    public ResponseEntity<Void> removeManagerFromTeam(
            @PathVariable UUID teamId
    ) {
        teamAdminService.removeManagerFromTeam(teamId);
        return ResponseEntity.ok().build();
    }

    public void archiveTeam(UUID teamId) {

    }

    public void addToTeam(UUID userId, UUID teamId) {

    }

    public void removeFromTeam(UUID userId, UUID teamId) {

    }
}
