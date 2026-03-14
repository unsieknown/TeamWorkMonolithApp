package com.mordiniaa.backend.payload.nodeDto;

import com.mordiniaa.backend.dto.file.FileNodeDto;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.payload.PageMeta;

import java.util.List;

public class CollectionNodeDtoResponse extends CollectionResponse<FileNodeDto> {

    public CollectionNodeDtoResponse(List<FileNodeDto> items, PageMeta pageMeta) {
        super(items, pageMeta);
    }
}
