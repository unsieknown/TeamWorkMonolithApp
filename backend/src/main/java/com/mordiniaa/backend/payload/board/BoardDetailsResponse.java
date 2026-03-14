package com.mordiniaa.backend.payload.board;

import com.mordiniaa.backend.dto.board.BoardShortDto;
import com.mordiniaa.backend.payload.APIResponse;

public class BoardDetailsResponse extends APIResponse<BoardShortDto> {

    public BoardDetailsResponse(String message, BoardShortDto data) {
        super(message, data);
    }
}
