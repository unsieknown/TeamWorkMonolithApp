package com.mordiniaa.backend.services.task;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mordiniaa.backend.dto.task.TaskDetailsDTO;
import com.mordiniaa.backend.dto.task.TaskShortDto;
import com.mordiniaa.backend.exceptions.*;
import com.mordiniaa.backend.exceptions.UnsupportedOperationException;
import com.mordiniaa.backend.mappers.task.TaskMapper;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.models.board.TaskCategory;
import com.mordiniaa.backend.models.task.Task;
import com.mordiniaa.backend.models.task.activity.TaskActivityElement;
import com.mordiniaa.backend.models.task.activity.TaskCategoryChange;
import com.mordiniaa.backend.models.task.activity.TaskComment;
import com.mordiniaa.backend.repositories.mongo.TaskRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.BoardAggregationRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardWithTaskCategories;
import com.mordiniaa.backend.request.task.UpdateTaskPositionRequest;
import com.mordiniaa.backend.request.task.UploadCommentRequest;
import com.mordiniaa.backend.utils.BoardUtils;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskActivityService {

    private final BoardAggregationRepository boardAggregationRepository;
    private final BoardUtils boardUtils;
    private final TaskRepository taskRepository;
    private final MongoTemplate mongoTemplate;
    private final TaskMapper taskMapper;
    private final TaskService taskService;

    @Transactional
    public TaskShortDto changeTaskPosition(UUID userId, String bId, String tId, UpdateTaskPositionRequest request) {

        BiFunction<ObjectId, ObjectId, BoardWithTaskCategories> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardForTaskWithCategories(boardId, userId, taskId)
                        .orElseThrow(() -> new BoardNotFoundException("Board Not Found")
                        );

        BiFunction<BoardWithTaskCategories, ObjectId, TaskShortDto> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);

            if (!currentMember.canViewBoard())
                throw new AccessDeniedException("You do not have permission to perform this operation");

            TaskCategory taskCategory = board.getTaskCategories()
                    .stream()
                    .filter(tC -> tC.getTasks().contains(taskId))
                    .findFirst()
                    .orElseThrow(() -> new TaskCategoryNotFoundException("Task Category Not Found"));

            Task task = taskService.findTaskById(taskId);

            UUID boardOwner = board.getOwner().getUserId();
            if (task.getCreatedBy().equals(boardOwner) && !userId.equals(boardOwner))
                throw new AccessDeniedException("You do not have permission to perform this operation");

            PositionUpdate positionUpdate = new PositionUpdate(mongoTemplate);
            if (request.getNewTaskCategory() != null) {

                if (!currentMember.canMoveTaskBetweenCategories())
                    throw new AccessDeniedException("You do not have permission to perform this operation");

                String newCategory = request.getNewTaskCategory();
                TaskCategory newTaskCategory = board.getTaskCategories()
                        .stream()
                        .filter(tC -> tC.getCategoryName().equals(newCategory))
                        .findFirst()
                        .orElseThrow(() -> new TaskCategoryNotFoundException("Task Category Not Found"));

                Update pollPushUpdate = new Update()
                        .pull("taskCategories.$[from].tasks", taskId)
                        .push("taskCategories.$[to].tasks", taskId);

                UpdateOptions options = new UpdateOptions().arrayFilters(List.of(
                        Filters.eq("from.categoryName", taskCategory.getCategoryName()),
                        Filters.eq("to.categoryName", newCategory)
                ));

                MongoCollection<Document> boardCollection = mongoTemplate.getCollection("boards");
                boardCollection.updateOne(
                        Filters.eq("_id", board.getId()),
                        pollPushUpdate.getUpdateObject(),
                        options
                );

                TaskCategoryChange taskCategoryChange = new TaskCategoryChange(userId);
                taskCategoryChange.setPrevCategory(taskCategory.getCategoryName());
                taskCategoryChange.setNextCategory(newCategory);
                task.addTaskActivityElement(taskCategoryChange);

                positionUpdate.moveBetweenCategories(taskCategory, newTaskCategory, task.getPositionInCategory(), request.getNewPosition());
            } else {

                if (task.getPositionInCategory() == request.getNewPosition())
                    throw new UnsupportedOperationException("Category is already at the specified position");

                if (task.getPositionInCategory() < request.getNewPosition()) {
                    positionUpdate.moveUpInCategory(taskCategory, task.getPositionInCategory(), request.getNewPosition());
                } else {
                    positionUpdate.moveDownInCategory(taskCategory, task.getPositionInCategory(), request.getNewPosition());
                }
            }
            positionUpdate.update();

            task.setPositionInCategory(request.getNewPosition());
            task = taskRepository.save(task);

            return taskMapper.toShortenedDto(task);
        };
        return taskService.executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    public TaskDetailsDTO writeComment(UUID userId, String bId, String tId, UploadCommentRequest uploadCommentRequest) {

        BiFunction<ObjectId, ObjectId, BoardWithTaskCategories> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardForTaskWithCategories(boardId, userId, taskId)
                        .orElseThrow(() -> new BoardNotFoundException("Board Not Found")
                        );

        BiFunction<BoardWithTaskCategories, ObjectId, TaskDetailsDTO> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);
            if (!currentMember.canCommentTask())
                throw new AccessDeniedException("You do not have permission to access this resource");

            Task task = taskService.findTaskById(taskId);

            boolean taskOwner = task.getCreatedBy().equals(currentMember.getUserId());
            boolean isAssigned = task.getAssignedTo().contains(currentMember.getUserId());
            boolean boardOwner = currentMember.getUserId().equals(board.getOwner().getUserId());
            if (!taskOwner && !isAssigned && !boardOwner)
                throw new AccessDeniedException("You do not have permission to access this resource");

            TaskComment taskComment = new TaskComment(userId);
            taskComment.setComment(uploadCommentRequest.getComment());
            task.addTaskActivityElement(taskComment);
            Task savedTask = taskRepository.save(task);

            Set<UUID> usersIds = savedTask.getActivityElements()
                    .stream().map(TaskActivityElement::getUser).collect(Collectors.toSet());
            return taskService.detailedTaskDto(task, usersIds);
        };
        return taskService.executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    public TaskDetailsDTO updateComment(UUID userId, String bId, String tId, UploadCommentRequest uploadCommentRequest) {

        if (uploadCommentRequest.getCommentId() == null)
            throw new BadRequestException("Comment Not Present");

        BiFunction<ObjectId, ObjectId, BoardWithTaskCategories> boardFunction = (boardId, taskId) ->
                boardAggregationRepository
                        .findBoardForTaskWithCategories(boardId, userId, taskId)
                        .orElseThrow(() -> new BoardNotFoundException("Board Not Found")
                        );

        BiFunction<BoardWithTaskCategories, ObjectId, TaskDetailsDTO> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);
            if (!currentMember.canViewBoard())
                throw new AccessDeniedException("You do not have permission to access this resource");

            Task task = taskService.findTaskById(taskId);
            TaskComment taskComment = getTaskComment(task, uploadCommentRequest.getCommentId());

            if (taskComment.isUpdated())
                throw new TaskAlreadyUpdatedException();

            if (!taskComment.getUser().equals(userId))
                throw new AccessDeniedException("You do not have permission to access this resource");

            if (!currentMember.canUpdateOwnComment())
                throw new AccessDeniedException("You do not have permission to access this resource");

            taskComment.setComment(uploadCommentRequest.getComment());
            taskComment.setUpdated(true);

            Task savedTask = taskRepository.save(task);

            Set<UUID> usersIds = savedTask.getActivityElements()
                    .stream().map(TaskActivityElement::getUser).collect(Collectors.toSet());
            return taskService.detailedTaskDto(task, usersIds);
        };
        return taskService.executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    public TaskDetailsDTO deleteComment(UUID userId, String bId, String tId, UUID commentId) {

        BiFunction<ObjectId, ObjectId, BoardWithTaskCategories> boardFunction =
                (boardId, taskId) -> boardAggregationRepository
                        .findBoardForTaskWithCategories(boardId, userId, taskId)
                        .orElseThrow(() -> new BoardNotFoundException("Board Not Found")
                        );

        BiFunction<BoardWithTaskCategories, ObjectId, TaskDetailsDTO> taskFunction = (board, taskId) -> {
            BoardMember currentMember = boardUtils.getBoardMember(board, userId);
            if (!currentMember.canViewBoard())
                throw new AccessDeniedException("You do not have permission to access this resource");

            Task task = taskService.findTaskById(taskId);
            TaskComment taskComment = getTaskComment(task, commentId);

            UUID boardOwner = board.getOwner().getUserId();
            UUID commentAuthor = taskComment.getUser();
            if (commentAuthor.equals(boardOwner) && !userId.equals(boardOwner))
                throw new AccessDeniedException("You do not have permission to access this resource");

            if (!currentMember.getUserId().equals(commentAuthor) && !currentMember.canDeleteAnyComment())
                throw new AccessDeniedException("You do not have permission to access this resource");

            boolean result = task.getActivityElements()
                    .removeIf(element -> element instanceof TaskComment tc && tc.getCommentId().equals(commentId));

            if (!result)
                throw new TaskCommentNotFound();

            Task savedTask = taskRepository.save(task);

            Set<UUID> usersIds = savedTask.getActivityElements()
                    .stream().map(TaskActivityElement::getUser).collect(Collectors.toSet());
            return taskService.detailedTaskDto(task, usersIds);
        };
        return taskService.executeTaskOperation(userId, bId, tId, boardFunction, taskFunction);
    }

    private TaskComment getTaskComment(Task task, UUID commentId) {
        return task.getActivityElements()
                .stream()
                .filter(taskActivityElement -> taskActivityElement instanceof TaskComment)
                .map(element -> (TaskComment) element)
                .filter(tC -> tC.getCommentId().equals(commentId))
                .findFirst()
                .orElseThrow(TaskCommentNotFound::new);
    }
}