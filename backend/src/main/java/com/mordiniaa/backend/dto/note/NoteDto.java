package com.mordiniaa.backend.dto.note;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(name = "Note Dto", description = "Schema To Hold Object Response Information")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NoteDto {

    @Schema(name = "id", description = "Note Id")
    @EqualsAndHashCode.Include
    private String id;

    @Schema(name = "title", description = "Note Title")
    private String title;

    @Schema(name = "ownerId", description = "Id Of The Owner")
    private UUID ownerId;

    @Schema(name = "content", description = "Note Content")
    private String content;

    @Schema(name = "createdAt", description = "Creation Date")
    private Instant createdAt;

    @Schema(name = "updatedAt", description = "Updated Date")
    private Instant updatedAt;
}
