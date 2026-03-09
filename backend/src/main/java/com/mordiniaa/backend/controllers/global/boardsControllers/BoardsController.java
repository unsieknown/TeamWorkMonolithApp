package com.mordiniaa.backend.controllers.global.boardsControllers;

import com.mordiniaa.backend.dto.board.BoardDetailsDto;
import com.mordiniaa.backend.dto.board.BoardShortDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.payload.PageMeta;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.board.BoardUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardsController {

    private final AuthUtils authUtils;
    private final BoardUserService boardUserService;

    public BoardsController(AuthUtils authUtils, BoardUserService boardUserService) {
        this.authUtils = authUtils;
        this.boardUserService = boardUserService;
    }

    @GetMapping
    public ResponseEntity<CollectionResponse<BoardShortDto>> getBoards(
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
                new CollectionResponse<>(
                        dtos,
                        pageMeta
                )
        );
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardShortDto>> getBoardDetails(
            @RequestParam("t") UUID teamId,
            @PathVariable String boardId) {

        UUID userId = authUtils.authenticatedUserId();
        BoardDetailsDto dto = boardUserService.getBoardDetails(userId, boardId, teamId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Success",
                        dto
                )
        );
    }
}
