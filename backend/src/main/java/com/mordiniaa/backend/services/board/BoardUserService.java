package com.mordiniaa.backend.services.board;

import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.dto.board.BoardShortDto;
import com.mordiniaa.backend.exceptions.AccessDeniedException;
import com.mordiniaa.backend.exceptions.BoardNotFoundException;
import com.mordiniaa.backend.mappers.board.BoardMapper;
import com.mordiniaa.backend.models.board.Board;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.BoardAggregationRepositoryImpl;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardFull;
import com.mordiniaa.backend.repositories.mongo.board.aggregation.returnTypes.BoardMembersOnly;
import com.mordiniaa.backend.services.user.MongoUserService;
import com.mordiniaa.backend.utils.BoardUtils;
import com.mordiniaa.backend.utils.MongoIdUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardUserService {

    private final MongoUserService mongoUserService;
    private final BoardAggregationRepositoryImpl boardAggregationRepositoryImpl;
    private final BoardMapper boardMapper;
    private final MongoIdUtils mongoIdUtils;
    private final BoardUtils boardUtils;

    public List<BoardShortDto> getBoardListForUser(UUID userId, UUID teamId) {

        mongoUserService.checkUserAvailability(userId);
        return boardAggregationRepositoryImpl.findAllBoardsForUserByUserIdAndTeamId(userId, teamId)
                .stream()
                .sorted(Comparator.comparing(Board::getUpdatedAt).reversed())
                .map(boardMapper::toShortDto)
                .toList();
    }

    public BoardDetailsDto getBoardDetails(UUID userId, String bId, UUID teamId) {

        mongoUserService.checkUserAvailability(userId);
        ObjectId boardId = mongoIdUtils.getObjectId(bId);

        BoardMembersOnly b = boardAggregationRepositoryImpl.findBoardMembers(boardId, userId, teamId)
                .orElseThrow(() -> new BoardNotFoundException("Board Not Found"));

        BoardMember currentMember = boardUtils.getBoardMember(b, userId);
        if (!currentMember.canViewBoard())
            throw new AccessDeniedException("You do not have permission to perform this operation");

        BoardFull board = boardAggregationRepositoryImpl.findBoardWithTasksByUserIdAndBoardIdAndTeamId(userId, boardId, teamId)
                .orElseThrow(() -> new BoardNotFoundException("Board Not Found"));
        return boardMapper.toDetailedDto(board);
    }
}
