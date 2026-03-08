package com.mordiniaa.backend.services.team;

import com.mordiniaa.backend.dto.team.TeamShortDto;
import com.mordiniaa.backend.mappers.team.TeamMapper;
import com.mordiniaa.backend.models.team.Team;
import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.repositories.mysql.TeamRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import com.mordiniaa.backend.request.team.TeamCreationRequest;
import com.mordiniaa.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamAdminService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TeamService teamService;
    private final TeamMapper teamMapper;
    private final UserRepository userRepository;

    @Transactional
    public TeamShortDto createTeam(TeamCreationRequest teamCreationRequest) {

        String teamName = teamCreationRequest.getTeamName().trim();
        String lowerTeamName = teamName.toLowerCase();
        if (teamRepository.existsByTeamNameIgnoreCase(lowerTeamName))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        Team team = new Team(lowerTeamName);
        team.setPresentationName(teamName);

        return teamMapper.toShortDto(teamRepository.save(team));
    }

    @Transactional
    public TeamShortDto assignManagerToTeam(UUID userId, UUID teamId) {

        User user = userService.findNonDeletedUserAndAppRole(userId, AppRole.ROLE_MANAGER);

        Team team = teamRepository.findTeamByTeamIdAndActiveTrue(teamId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        if (team.getManager() != null) {
            User manager = team.getManager();
            if (manager.getUserId().equals(userId))
                // This manager Already Assigned
                throw new RuntimeException(); // TODO: Change In Exceptions Section
            // Other Manager Already Assigned
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        team.setManager(user);
        return teamMapper.toShortDto(teamRepository.save(team));
    }

    @Transactional
    public void removeManagerFromTeam(UUID teamId) {

        Team team = teamService.getTeam(teamId);
        if (team.getManager() == null)
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        team.removeManager();
        teamRepository.save(team);
    }

    @Transactional
    public void archiveTeam(UUID teamId) {

        Team team = teamRepository.findTeamByTeamIdAndActiveTrue(teamId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        team.deactivate();
        teamRepository.save(team);
    }

    @Transactional
    public void addToTeam(UUID userId, UUID teamId) {

        Team team = teamRepository.findTeamByTeamIdAndActiveTrue(teamId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        User manager = team.getManager();
        if (manager != null && manager.getUserId().equals(userId))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        boolean isMember = team.getTeamMembers().stream()
                .anyMatch(user -> user.getUserId().equals(userId));
        if (isMember)
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        User user = userService.getUser(userId);
        team.addMember(user);

        teamRepository.save(team);
    }

    @Transactional
    public void removeFromTeam(UUID userId, UUID teamId) {

        Team team = teamService.getTeam(teamId);
        User manager = team.getManager();
        if (manager != null && manager.getUserId().equals(userId))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        User user = team.getTeamMembers().stream().filter(member -> member.getUserId().equals(userId))
                .findFirst().orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
        team.removeMember(user);

        teamRepository.save(team);
    }

    @Transactional
    public void removeFromTeamByEvent(UUID userId) {

        User user = userService.getUser(userId);
        if (user.getRole().getAppRole().equals(AppRole.ROLE_MANAGER))
            user.getOwnedTeams().forEach(t -> removeManagerFromTeam(t.getTeamId()));
        else
            user.getTeams().forEach(t -> removeFromTeam(userId, t.getTeamId()));
    }
}
