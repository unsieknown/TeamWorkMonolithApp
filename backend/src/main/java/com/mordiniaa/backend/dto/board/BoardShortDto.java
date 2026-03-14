package com.mordiniaa.backend.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        name = "Board Short Dto",
        description = "Short Dto Containing Only Most Important Fields"
)
public class BoardShortDto {

    @Schema(name = "boardId", description = "Id Of The Specific Board")
    private String boardId;

    @Schema(name = "boardName", description = "Name Of The Board")
    private String boardName;
}
