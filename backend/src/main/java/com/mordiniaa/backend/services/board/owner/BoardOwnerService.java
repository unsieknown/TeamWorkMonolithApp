package com.mordiniaa.backend.services.board.owner;

import com.mongodb.client.result.UpdateResult;
import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.exceptions.BoardNotFoundException;
import com.mordiniaa.backend.exceptions.TeamNotFoundException;
import com.mordiniaa.backend.exceptions.UserNotInTeamException;
import com.mordiniaa.backend.mappers.board.BoardMapper;
import com.mordiniaa.backend.models.board.Board;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.models.board.permissions.BoardPermission;
import com.mordiniaa.backend.repositories.mongo.board.BoardRepository;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.BoardAggregationRepositoryImpl;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardFull;
import com.mordiniaa.backend.repositories.mysql.TeamRepository;
import com.mordiniaa.backend.request.board.BoardCreationRequest;
import com.mordiniaa.backend.services.board.admin.BoardAdminService;
import com.mordiniaa.backend.services.user.MongoUserService;
import com.mordiniaa.backend.utils.MongoIdUtils;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardOwnerService {

    private final MongoUserService mongoUserService;
    private final TeamRepository teamRepository;
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final BoardAggregationRepositoryImpl boardAggregationRepositoryImpl;
    private final MongoIdUtils mongoIdUtils;
    private final MongoTemplate mongoTemplate;
    private final BoardAdminService boardAdminService;

    public BoardDetailsDto createBoard(UUID userId, BoardCreationRequest boardCreationRequest) {

        mongoUserService.checkUserAvailability(userId);
        UUID teamId = boardCreationRequest.getTeamId();

        if (!teamRepository.existsTeamByTeamIdAndManager_UserId(teamId, userId))
            throw new TeamNotFoundException("Team Not Found");

        Board board = new Board();
        board.setTeamId(teamId);
        board.setBoardName(boardCreationRequest.getBoardName());

        BoardMember ownerMember = boardAdminService.createBoardOwner(userId);

        board.setOwner(ownerMember);

        Board savedBoard = boardRepository.save(board);
        BoardFull aggregatedBoardDocument = boardAggregationRepositoryImpl
                .findBoardWithTasksByUserIdAndBoardIdAndTeamId(userId, savedBoard.getId(), teamId)
                .orElseThrow(() -> new BoardNotFoundException("Board Not Found"));

        return boardMapper.toDetailedDto(aggregatedBoardDocument);
    }

    public BoardDetailsDto addUserToBoard(UUID boardOwner, UUID userId, String bId) {

        mongoUserService.checkUserAvailability(boardOwner, userId);
        ObjectId boardId = mongoIdUtils.getObjectId(bId);

        Board board = boardAggregationRepositoryImpl.findFullBoardByIdAndOwner(boardId, boardOwner)
                .orElseThrow(() -> new BoardNotFoundException("Board Not Found"));

        UUID teamId = board.getTeamId();
        if (!teamRepository.existsUserInTeam(teamId, userId))
            throw new UserNotInTeamException("User Not Found In Team");

        BoardMember newMember = new BoardMember(userId);
        newMember.setBoardPermissions(Set.of(BoardPermission.VIEW_BOARD));

        board.addMember(newMember);
        boardRepository.save(board);
        BoardFull savedBoard = boardAggregationRepositoryImpl
                .findBoardWithTasksByUserIdAndBoardIdAndTeamId(boardOwner, boardId, teamId)
                .orElseThrow(BoardNotFoundException::new);
        return boardMapper.toDetailedDto(savedBoard);
    }

    public BoardDetailsDto removeUserFromBoard(UUID boardOwner, UUID userId, String bId) {

        mongoUserService.checkUserAvailability(boardOwner);
        ObjectId boardId = mongoIdUtils.getObjectId(bId);

        Board board = boardAggregationRepositoryImpl.findFullBoardByIdAndOwner(boardId, boardOwner)
                .orElseThrow(() -> new BoardNotFoundException("Board Not Found"));
        board.removeMember(userId);
        boardRepository.save(board);
        BoardFull savedBoard = boardAggregationRepositoryImpl
                .findBoardWithTasksByUserIdAndBoardIdAndTeamId(boardOwner, boardId, board.getTeamId())
                .orElseThrow(BoardNotFoundException::new);
        return boardMapper.toDetailedDto(savedBoard);
    }

    public void deleteBoard(UUID boardOwner, String bId) {

        mongoUserService.checkUserAvailability(boardOwner);
        ObjectId boardId = mongoIdUtils.getObjectId(bId);

        Update update = new Update()
                .set("archived", true)
                .set("deleted", true);
        Query updateQuery = Query.query(
                new Criteria().andOperator(
                        Criteria.where("_id").is(boardId),
                        Criteria.where("owner.userId").is(boardOwner)
                )
        );

        UpdateResult result = mongoTemplate.updateFirst(updateQuery, update, Board.class);
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException("Resource not found");
    }
}
