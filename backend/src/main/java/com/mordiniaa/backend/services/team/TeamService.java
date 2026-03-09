package com.mordiniaa.backend.services.team;

import com.mordiniaa.backend.dto.team.TeamDetailedDto;
import com.mordiniaa.backend.dto.team.TeamShortDto;
import com.mordiniaa.backend.dto.user.UserDto;
import com.mordiniaa.backend.mappers.team.TeamMapper;
import com.mordiniaa.backend.mappers.user.UserMapper;
import com.mordiniaa.backend.models.team.Team;
import com.mordiniaa.backend.repositories.mysql.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final UserMapper userMapper;

    Team getTeam(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
    }

    public List<TeamShortDto> getTeamsForManager(UUID managerId) {
        return teamRepository.findAllByManager_UserId(managerId).stream()
                .map(teamMapper::toShortDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamDetailedDto getTeamDetails(UUID teamId, UUID managerId) {

        Team team = teamRepository.findTeamByTeamIdAndManager_UserId(teamId, managerId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        Set<UserDto> dtoMembers = team.getTeamMembers().stream().map(userMapper::toDto)
                .collect(Collectors.toSet());

        TeamDetailedDto teamDetailedDto = (TeamDetailedDto) teamMapper.toShortDto(team);
        teamDetailedDto.setTeamMembers(dtoMembers);
        return teamDetailedDto;
    }
}
