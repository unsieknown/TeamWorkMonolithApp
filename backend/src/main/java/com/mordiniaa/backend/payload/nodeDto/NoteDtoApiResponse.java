package com.mordiniaa.backend.payload.nodeDto;

import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.payload.APIResponse;

public class NoteDtoApiResponse extends APIResponse<NoteDto> {

    public NoteDtoApiResponse(String message, NoteDto data) {
        super(message, data);
    }
}
