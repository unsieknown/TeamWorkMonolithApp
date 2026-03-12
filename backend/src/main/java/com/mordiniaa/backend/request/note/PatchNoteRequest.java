package com.mordiniaa.backend.request.note;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mordiniaa.backend.request.note.deadline.PatchDeadlineNoteRequest;
import com.mordiniaa.backend.request.note.regular.PatchRegularNoteRequest;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatchRegularNoteRequest.class, name = "REGULAR"),
        @JsonSubTypes.Type(value = PatchDeadlineNoteRequest.class, name = "DEADLINE")
})
public class PatchNoteRequest implements NoteRequest {

    @Pattern(regexp = "^[\\p{L}0-9 .,_!?()\\-<>]{3,40}$")
    private String title;
    private String content;
}
