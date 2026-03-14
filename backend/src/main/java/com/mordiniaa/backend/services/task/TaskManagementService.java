package com.mordiniaa.backend.services.task;

import com.mordiniaa.backend.dto.task.TaskDetailsDTO;
import com.mordiniaa.backend.dto.task.TaskShortDto;
import com.mordiniaa.backend.exceptions.AccessDeniedException;
import com.mordiniaa.backend.exceptions.BadRequestException;
import com.mordiniaa.backend.exceptions.BoardNotFoundException;
import com.mordiniaa.backend.exceptions.UsersNotAvailableException;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.models.task.Task;
import com.mordiniaa.backend.models.task.activity.TaskActivityElement;
import com.mordiniaa.backend.repositories.mongo.TaskRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.BoardAggregationRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardMembersOnly;
import com.mordiniaa.backend.repositories.mongo.user.aggregation.UserReprCustomRepository;
import com.mordiniaa.backend.request.task.AssignUsersRequest;
import com.mordiniaa.backend.request.task.PatchTaskDataRequest;
import com.mordiniaa.backend.utils.BoardUtils;
import com.mordiniaa.backend.utils.MongoIdUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskManagementService {

    private final MongoIdUtils mongoIdUtils;
    private final BoardAggregationRepository boardAggregationRepository;
    private final BoardUtils boardUtils;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final UserReprCustomRepository userReprCustomRepository;

    public TaskDetailsDTO updateTask(UUID userId, String bId, String tId, PatchTaskDataRequest patchRequest) {

        BiFunction<ObjectId, ObjectId, BoardMembersOnly> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardMembersForTask(boardId, userId, taskId)
                        .orElseThrow(BoardNotFoundException::new);

        BiFunction<BoardMembersOnly, ObjectId, TaskDetailsDTO> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);
            Task task = taskService.findTaskById(taskId);

            UUID boardOwner = board.getOwner().getUserId();
            UUID taskAuthor = task.getCreatedBy();

            if (!userId.equals(boardOwner) && !userId.equals(taskAuthor)) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }

            if (!task.getCreatedBy().equals(userId) && !currentMember.canUpdateTask()) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }

            if (patchRequest.getTitle() != null && !patchRequest.getTitle().isBlank())
                task.setTitle(patchRequest.getTitle());

            if (patchRequest.getDescription() != null && !patchRequest.getDescription().isBlank())
                task.setDescription(patchRequest.getDescription());

            if (patchRequest.getDeadline() != null && patchRequest.getDeadline().isAfter(Instant.now()))
                task.setDeadline(patchRequest.getDeadline());

            Task savedTask = taskRepository.save(task);

            Set<UUID> usersIds = savedTask.getActivityElements()
                    .stream().map(TaskActivityElement::getUser).collect(Collectors.toSet());
            return taskService.detailedTaskDto(savedTask, usersIds);
        };

        return taskService.executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    public TaskDetailsDTO assignUsersToTask(UUID assigningId, AssignUsersRequest assignRequest, String bId, String tId) {

        Set<UUID> toAssign = assignRequest.getUsers();

        Set<UUID> usersToCheck = new HashSet<>(toAssign);
        usersToCheck.add(assigningId);

        boolean result = userReprCustomRepository.allUsersAvailable(usersToCheck);
        if (!result)
            throw new UsersNotAvailableException();

        ObjectId boardId = mongoIdUtils.getObjectId(bId);
        ObjectId taskId = mongoIdUtils.getObjectId(tId);

        BoardMembersOnly board = boardAggregationRepository
                .findBoardMembersForTask(boardId, assigningId, taskId)
                .orElseThrow(BoardNotFoundException::new);

        BoardMember currentUser = boardUtils.getBoardMember(board, assigningId);
        if (!currentUser.canViewBoard())
            throw new AccessDeniedException("You do not have permission to access this resource");

        Task task = taskService.findTaskById(taskId);

        UUID taskOwner = task.getCreatedBy();
        UUID boardOwner = board.getOwner().getUserId();

        boolean isTaskOwner = currentUser.getUserId().equals(taskOwner);
        boolean isBoardOwner = currentUser.getUserId().equals(board.getOwner().getUserId());

        if (!isTaskOwner && !isBoardOwner) {
            if (toAssign.contains(boardOwner) || toAssign.contains(taskOwner))
                throw new AccessDeniedException("You do not have permission to access this resource");
            if (!currentUser.canAssignTask())
                throw new AccessDeniedException("You do not have permission to access this resource");
        }

        if (!currentUser.getUserId().equals(boardOwner) && toAssign.contains(boardOwner))
            throw new AccessDeniedException("You do not have permission to access this resource");

        Set<UUID> assigned = new HashSet<>(task.getAssignedTo());
        assigned.retainAll(toAssign);
        if (!assigned.isEmpty()) {
            throw new UsersNotAvailableException("One or more users are already assigned");
        }

        Set<UUID> membersIds = board.getMembers().stream()
                .map(BoardMember::getUserId)
                .collect(Collectors.toSet());
        membersIds.add(assigningId);

        if (!membersIds.containsAll(toAssign))
            throw new UsersNotAvailableException("One or more users are not part of board members");

        task.addMembers(toAssign);

        Task savedTask = taskRepository.save(task);
        Set<UUID> usersIds = savedTask.getActivityElements()
                .stream().map(TaskActivityElement::getUser).collect(Collectors.toSet());

        return taskService.detailedTaskDto(task, usersIds);
    }

    public void removeUserFromTask(UUID userId, UUID toDeleteId, String bId, String tId) {

        BiFunction<ObjectId, ObjectId, BoardMembersOnly> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardMembersForTask(boardId, userId, taskId)
                        .orElseThrow(BoardNotFoundException::new);

        BiFunction<BoardMembersOnly, ObjectId, TaskShortDto> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);
            if (!currentMember.canViewBoard())
                throw new AccessDeniedException("You do not have permission to access this resource");

            Task task = taskService.findTaskById(taskId);

            UUID taskOwner = task.getCreatedBy();
            UUID boardOwner = board.getOwner().getUserId();

            boolean isTaskOwner = currentMember.getUserId().equals(taskOwner);
            boolean isBoardOwner = currentMember.getUserId().equals(board.getOwner().getUserId());

            if (!isTaskOwner && !isBoardOwner) {
                if (toDeleteId.equals(taskOwner) || toDeleteId.equals(boardOwner))
                    throw new AccessDeniedException("You are not owner of this resource");
                if (!currentMember.canUnassignTask())
                    throw new AccessDeniedException("You do not have permission to access this resource");
            }

            if (!currentMember.getUserId().equals(boardOwner) && toDeleteId.equals(boardOwner))
                throw new AccessDeniedException("You don't have permission to remove these user from task");

            if (!task.getAssignedTo().contains(toDeleteId))
                throw new UsersNotAvailableException("User Not Assigned To This Task");

            task.removeMember(toDeleteId);

            taskRepository.save(task);
            return null;
        };
        taskService.executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }
}
