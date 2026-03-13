package com.mordiniaa.backend.utils;

import com.mordiniaa.backend.exceptions.BadRequestException;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.models.board.BoardMembers;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoardUtils {

    public BoardMember getBoardMember(BoardMembers board, UUID userId) {
        if (board.getOwner().getUserId().equals(userId)) {
            return board.getOwner();
        } else {
            return board.getMembers()
                    .stream()
                    .filter(bm -> bm.getUserId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Board Member Not Found"));
        }
    }
}
