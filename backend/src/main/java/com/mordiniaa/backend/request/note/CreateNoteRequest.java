package com.mordiniaa.backend.request.note;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mordiniaa.backend.request.note.deadline.CreateDeadlineNoteRequest;
import com.mordiniaa.backend.request.note.regular.CreateRegularNoteRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateRegularNoteRequest.class, name = "REGULAR"),
        @JsonSubTypes.Type(value = CreateDeadlineNoteRequest.class, name = "DEADLINE")
})
public class CreateNoteRequest implements NoteRequest {

    @NotBlank(message = "Title is required")
    @Pattern(regexp = "^[\\p{L}0-9 .,_!?()\\-<>]+$")
    @Size(min = 3, max = 40, message = "Title must be between 3 and 40 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 512, message = "Content max length is 512 characters")
    private String content;
}
