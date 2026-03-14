package com.mordiniaa.backend.payload.note;

import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.payload.PageMeta;

import java.util.List;

public class NoteDtoCollectionResponse extends CollectionResponse<NoteDto> {
    public NoteDtoCollectionResponse(List<NoteDto> items, PageMeta pageMeta) {
        super(items, pageMeta);
    }
}
