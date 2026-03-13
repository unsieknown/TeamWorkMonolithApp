package com.mordiniaa.backend.services.task;

import com.mordiniaa.backend.dto.task.TaskDetailsDTO;
import com.mordiniaa.backend.dto.task.TaskShortDto;
import com.mordiniaa.backend.exceptions.AccessDeniedException;
import com.mordiniaa.backend.exceptions.BoardNotFoundException;
import com.mordiniaa.backend.exceptions.TaskNotFoundException;
import com.mordiniaa.backend.exceptions.UsersNotAvailableException;
import com.mordiniaa.backend.mappers.task.TaskMapper;
import com.mordiniaa.backend.models.board.Board;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.models.board.BoardMembers;
import com.mordiniaa.backend.models.board.BoardTemplate;
import com.mordiniaa.backend.models.user.mongodb.UserRepresentation;
import com.mordiniaa.backend.models.task.Task;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.BoardAggregationRepository;
import com.mordiniaa.backend.repositories.mongo.board.BoardRepository;
import com.mordiniaa.backend.repositories.mongo.TaskRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardMembersOnly;
import com.mordiniaa.backend.repositories.mongo.user.UserRepresentationRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardMembersTasksOnly;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.TaskCreatorProjectionWithOptPosition;
import com.mordiniaa.backend.request.task.CreateTaskRequest;
import com.mordiniaa.backend.services.user.MongoUserService;
import com.mordiniaa.backend.utils.BoardUtils;
import com.mordiniaa.backend.utils.MongoIdUtils;
import lombok.*;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepresentationRepository userRepresentationRepository;
    private final BoardRepository boardRepository;
    private final BoardAggregationRepository boardAggregationRepository;
    private final TaskRepository taskRepository;
    private final MongoTemplate mongoTemplate;
    private final TaskMapper taskMapper;
    private final MongoUserService mongoUserService;
    private final MongoIdUtils mongoIdUtils;
    private final BoardUtils boardUtils;

    protected Task findTaskById(ObjectId taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(TaskNotFoundException::new);
    }

    public TaskDetailsDTO getTaskDetailsById(UUID userId, String bId, String tId) {

        BiFunction<ObjectId, ObjectId, BoardMembersOnly> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardMembersForTask(boardId, userId, taskId)
                        .orElseThrow(BoardNotFoundException::new);

        BiFunction<BoardMembers, ObjectId, TaskShortDto> taskFunction = (board, taskId) -> {

            Set<BoardMember> allMembers = new HashSet<>(board.getMembers());
            allMembers.add(board.getOwner());

            BoardMember currentMember = allMembers.stream().filter(mb -> mb.getUserId().equals(userId))
                    .findFirst().orElseThrow(() -> new UsersNotAvailableException("User Not Found"));

            if (!currentMember.canViewBoard()) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(TaskNotFoundException::new);

            Set<UUID> userIds = allMembers.stream().map(BoardMember::getUserId)
                    .collect(Collectors.toSet());

            return detailedTaskDto(task, userIds);
        };
        return (TaskDetailsDTO) executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    @Transactional
    public TaskShortDto createTask(UUID userId, String bId, String categoryName, CreateTaskRequest createTaskRequest) {

        BiFunction<ObjectId, ObjectId, Board> boardFunction =
                (boardId, taskId) -> boardRepository
                        .getBoardByIdWithCategoryAndBoardMemberOrOwner(boardId, categoryName, userId)
                        .orElseThrow(BoardNotFoundException::new);

        BiFunction<Board, ObjectId, TaskShortDto> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);
            if (!board.getOwner().getUserId().equals(userId)
                    && createTaskRequest.getAssignedTo().contains(board.getOwner().getUserId())) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }

            if (!currentMember.canCreateTask())
                throw new AccessDeniedException("You do not have permission to access this resource");

            Task task = new Task();
            if (createTaskRequest.getAssignedTo() != null) {

                Set<UUID> assignedTo = new HashSet<>(createTaskRequest.getAssignedTo());

                if (assignedTo.contains(currentMember.getUserId())) {
                    task.addMember(currentMember.getUserId());
                    assignedTo.remove(currentMember.getUserId());
                }

                if (!assignedTo.isEmpty()) {
                    if (!currentMember.canAssignTask())
                        throw new AccessDeniedException("You do not have permission to access this resource");

                    Set<UUID> membersIds = board.getMembers().stream().map(BoardMember::getUserId)
                            .collect(Collectors.toSet());
                    if (!membersIds.containsAll(assignedTo)) {
                        throw new UsersNotAvailableException("One or more users are not part of board members");
                    }
                    task.addMembers(assignedTo);
                }
            }

            task.setCreatedBy(currentMember.getUserId());
            task.setTitle(createTaskRequest.getTitle());
            task.setDescription(createTaskRequest.getDescription());
            task.setDeadline(createTaskRequest.getDeadline());
            task.setPositionInCategory(0);

            Set<ObjectId> taskIds = board.getTaskCategories().getFirst().getTasks();
            if (!taskIds.isEmpty()) {
                Query query = new Query(
                        Criteria.where("_id").in(taskIds)
                );

                Update update = new Update()
                        .inc("positionInCategory", 1);

                mongoTemplate.updateMulti(query, update, Task.class);
            }

            Task savedTask = taskRepository.save(task);
            board.getTaskCategories().getFirst().addTaskId(savedTask.getId());
            mongoTemplate.updateFirst(Query.query(
                            Criteria.where("_id").is(board.getId())
                                    .and("taskCategories.categoryName").is(categoryName)
                    ),
                    new Update().push("taskCategories.$.tasks", savedTask.getId()),
                    Board.class);
            return taskMapper.toShortenedDto(savedTask);
        };

        return executeTaskOperation(userId, bId, null, boardFunction, taskFunction);
    }

    public void deleteTaskFromBoard(UUID userId, String bId, String tId) {

        BiFunction<ObjectId, ObjectId, BoardMembersTasksOnly> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardForTaskWithCategory(boardId, userId, taskId)
                        .orElseThrow(BoardNotFoundException::new);

        BiFunction<BoardMembersTasksOnly, ObjectId, TaskShortDto> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);

            List<TaskCreatorProjectionWithOptPosition> tasks = board.getTasks();
            TaskCreatorProjectionWithOptPosition task = tasks
                    .stream()
                    .filter(t -> t.getId().equals(taskId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Resource Not Found"));

            if (task.getTaskPosition() == null) {
                throw new IllegalStateException("Task position not resolved");
            }

            UUID taskAuthor = task.getCreatedBy();
            UUID boardOwner = board.getOwner().getUserId();

            if (!taskAuthor.equals(userId)) {
                if (taskAuthor.equals(boardOwner))
                    throw new AccessDeniedException("You do not have permission to access this resource");

                if (!currentMember.canDeleteTask())
                    throw new AccessDeniedException("You do not have permission to access this resource");
            }

            Query pullQuery = Query.query(Criteria
                    .where("_id").is(board.getId())
                    .and("taskCategories.tasks").is(taskId));
            Update pullUpdate = new Update()
                    .pull("taskCategories.$[].tasks", taskId);
            mongoTemplate.updateFirst(pullQuery, pullUpdate, Board.class);
            taskRepository.deleteById(taskId);

            Query decQuery = Query.query(Criteria
                    .where("_id").in(tasks.stream().map(TaskCreatorProjectionWithOptPosition::getId).collect(Collectors.toSet()))
                    .and("positionInCategory").gt(task.getTaskPosition())
            );
            Update decUpdate = new Update()
                    .inc("positionInCategory", -1);
            mongoTemplate.updateMulti(decQuery, decUpdate, Task.class);
            return null;
        };

        executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    protected TaskDetailsDTO detailedTaskDto(Task task, Set<UUID> userIds) {

        Map<UUID, UserRepresentation> users = userRepresentationRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(
                        UserRepresentation::getUserId,
                        Function.identity()
                ));

        return taskMapper.toDetailedDto(task, users);
    }

    protected <B extends BoardMembers & BoardTemplate, R extends TaskShortDto> R executeTaskOperation(
            UUID userId,
            String bId,
            String tId,
            BiFunction<ObjectId, ObjectId, B> boardFunction,
            BiFunction<? super B, ObjectId, R> taskFunction
    ) {

        mongoUserService.checkUserAvailability(userId);

        ObjectId boardId = mongoIdUtils.getObjectId(bId);
        ObjectId taskId = null;

        if (tId != null) {
            taskId = mongoIdUtils.getObjectId(tId);
        }

        B board = boardFunction.apply(boardId, taskId);

        return taskFunction.apply(board, taskId);
    }
}
