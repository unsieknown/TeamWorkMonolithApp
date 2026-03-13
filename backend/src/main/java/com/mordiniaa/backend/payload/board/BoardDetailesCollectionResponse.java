package com.mordiniaa.backend.payload.board;

import com.mordiniaa.backend.dto.board.BoardShortDto;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.payload.PageMeta;

import java.util.List;

public class BoardDetailesCollectionResponse extends CollectionResponse<BoardShortDto> {
    public BoardDetailesCollectionResponse(List<BoardShortDto> items, PageMeta pageMeta) {
        super(items, pageMeta);
    }
}
