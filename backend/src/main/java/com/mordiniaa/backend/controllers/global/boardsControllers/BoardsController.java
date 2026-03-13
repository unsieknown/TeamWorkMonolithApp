package com.mordiniaa.backend.controllers.global.boardsControllers;

import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.dto.board.BoardShortDto;
import com.mordiniaa.backend.payload.*;
import com.mordiniaa.backend.payload.board.BoardDetailesCollectionResponse;
import com.mordiniaa.backend.payload.board.BoardDetailsResponse;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.board.BoardUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Read APIs for Fetching Boards List Or Board Details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardsController {

    private final AuthUtils authUtils;
    private final BoardUserService boardUserService;

    @Operation(
            summary = "Get Boards List",
            description = "Returns List Of Short Board Dto In Collection Response"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List Of Board Fetched", content = @Content(
                    schema = @Schema(implementation = BoardDetailesCollectionResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access Denied", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @GetMapping
    public ResponseEntity<BoardDetailesCollectionResponse> getBoards(
            @Parameter(in = ParameterIn.QUERY, name = "t", description = "Team Id", schema = @Schema(implementation = UUID.class), required = true)
            @RequestParam("t") UUID teamId
    ) {

        UUID userId = authUtils.authenticatedUserId();
        List<BoardShortDto> dtos = boardUserService.getBoardListForUser(userId, teamId);

        PageMeta pageMeta = new PageMeta();
        pageMeta.setLastPage(true);
        pageMeta.setPage(0);
        pageMeta.setTotalPages(1);
        pageMeta.setSize(dtos.size());
        pageMeta.setTotalItems(dtos.size());

        return ResponseEntity.ok(
                new BoardDetailesCollectionResponse(
                        dtos,
                        pageMeta
                )
        );
    }

    @Operation(
            summary = "Get Board Details",
            description = "Returns Details Of Specified Board"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Board Details", content = @Content(
                    schema = @Schema(implementation = BoardDetailsResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied or insufficient permissions", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Board Not Found", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailsResponse> getBoardDetails(
            @Parameter(in = ParameterIn.QUERY, name = "t", description = "Team Id", schema = @Schema(implementation = UUID.class), required = true)
            @RequestParam("t") UUID teamId,
            @Parameter(in = ParameterIn.PATH, name = "boardId", description = "Board Id", schema = @Schema(implementation = String.class), required = true)
            @PathVariable String boardId
    ) {
        UUID userId = authUtils.authenticatedUserId();
        BoardDetailsDto dto = boardUserService.getBoardDetails(userId, boardId, teamId);
        return ResponseEntity.ok(
                new BoardDetailsResponse(
                        "Success",
                        dto
                )
        );
    }
}
