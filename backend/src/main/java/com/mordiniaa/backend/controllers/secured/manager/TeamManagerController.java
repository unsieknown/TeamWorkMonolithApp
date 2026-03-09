package com.mordiniaa.backend.controllers.secured.manager;

import com.mordiniaa.backend.dto.team.TeamDetailedDto;
import com.mordiniaa.backend.dto.team.TeamShortDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.payload.PageMeta;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.team.TeamService;
import com.mordiniaa.backend.utils.PageResult;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager/team")
public class TeamManagerController {

    private final AuthUtils authUtils;
    private final TeamService teamService;


    @GetMapping
    public ResponseEntity<CollectionResponse<TeamShortDto>> getTeams() {

        UUID managerId = authUtils.authenticatedUserId();
        List<TeamShortDto> dtos = teamService.getTeamsForManager(managerId);
        PageMeta pageMeta = new PageMeta(
                0,
                dtos.size(),
                dtos.size(),
                1,
                true
        );
        return ResponseEntity.ok(
                new CollectionResponse<>(
                        dtos,
                        pageMeta
                )
        );
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamDetailedDto>> getTeamDetails(@PathVariable UUID teamId) {

        UUID managerId = authUtils.authenticatedUserId();
        TeamDetailedDto detailedDto = teamService.getTeamDetails(teamId, managerId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Success",
                        detailedDto
                )
        );
    }
}
