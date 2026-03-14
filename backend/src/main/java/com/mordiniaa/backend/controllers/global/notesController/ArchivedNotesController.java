package com.mordiniaa.backend.controllers.global.notesController;

import com.mordiniaa.backend.config.NotesConstants;
import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.payload.APIResponse;
import com.mordiniaa.backend.payload.note.NoteDtoCollectionResponse;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.notes.ArchivedNotesService;
import com.mordiniaa.backend.utils.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Archive Note Controller",
        description = "Controller For Archived Notes Management"
)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notes/archive")
public class ArchivedNotesController {

    private final ArchivedNotesService archivedNotesService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "Fetch All Archived Notes"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fetch All Archived Notes",
                    content = @Content(schema = @Schema(implementation = NoteDtoCollectionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            )
    })
    @GetMapping
    public ResponseEntity<NoteDtoCollectionResponse> getAllArchivedNotesForUser(
            @Parameter(in = ParameterIn.QUERY, description = "Page Number Positive Or Zero", schema = @Schema(implementation = Integer.class))
            @RequestParam(name = "pn", required = false, defaultValue = NotesConstants.PAGE_NUMBER) @PositiveOrZero int pageNumber,

            @Parameter(in = ParameterIn.QUERY, description = "Page Size Positive Max 50", schema = @Schema(implementation = Integer.class))
            @RequestParam(name = "ps", required = false, defaultValue = NotesConstants.PAGE_SIZE) @Positive @Max(50) int pageSize
    ) {
        UUID userId = authUtils.authenticatedUserId();
        PageResult<List<NoteDto>> result = archivedNotesService.fetchAllArchivedNotes(userId, pageNumber, pageSize);
        return ResponseEntity.ok(new NoteDtoCollectionResponse(result.getData(), result.getPageMeta()));
    }

    @Operation(
            summary = "Switch Archived Status",
            description = "Changes Archived Status For Note (ON/OFF)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully Changed Archived Status"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid Note Id"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note Not Found"
            )
    })
    @PutMapping("/{noteId}")
    public ResponseEntity<APIResponse<Void>> switchArchived(@PathVariable String noteId) {

        UUID userId = authUtils.authenticatedUserId();
        archivedNotesService.switchArchivedNoteForUser(userId, noteId);
        return ResponseEntity.ok(
                new APIResponse<>(
                        "Archive Status Switched Successfully",
                        null
                )
        );
    }
}
