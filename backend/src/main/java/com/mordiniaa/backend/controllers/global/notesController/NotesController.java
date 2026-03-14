package com.mordiniaa.backend.controllers.global.notesController;

import com.mordiniaa.backend.config.NotesConstants;
import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.payload.APIExceptionResponse;
import com.mordiniaa.backend.payload.nodeDto.NoteDtoApiResponse;
import com.mordiniaa.backend.payload.note.NoteDtoCollectionResponse;
import com.mordiniaa.backend.request.note.CreateNoteRequest;
import com.mordiniaa.backend.request.note.PatchNoteRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.notes.NotesService;
import com.mordiniaa.backend.utils.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Notes Controller",
        description = "Notes For Notes Management"
)
@Validated
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NotesController {

    private final NotesService notesService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "Get Note",
            description = "Get Note Details By Note Id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get Not Details By Not Id", content = @Content(
                    schema = @Schema(implementation = NoteDtoApiResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access Denied", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Note Not Found", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDtoApiResponse> getNoteById(
            @Parameter(in = ParameterIn.PATH, required = true, name = "noteId", description = "Note Id", schema = @Schema(implementation = String.class))
            @PathVariable String noteId
    ) {

        UUID userId = authUtils.authenticatedUserId();
        return notesService.getNoteById(
                        noteId,
                        userId
                )
                .map(dto -> ResponseEntity.ok(new NoteDtoApiResponse("Success", dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fetch All Non Archived Notes", content = @Content(
                    schema = @Schema(implementation = NoteDtoCollectionResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Invalid Parameter", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access Denied", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @GetMapping
    public ResponseEntity<NoteDtoCollectionResponse> fetchAllNotesForUser(
            @Parameter(in = ParameterIn.QUERY, name = "pn", description = "Page Number Positive Or Zero", schema = @Schema(implementation = Integer.class))
            @RequestParam(name = "pn", required = false, defaultValue = NotesConstants.PAGE_NUMBER) @PositiveOrZero int pageNumber,

            @Parameter(in = ParameterIn.QUERY, name = "ps", description = "Page Size Positive Max 50", schema = @Schema(implementation = Integer.class))
            @RequestParam(name = "ps", required = false, defaultValue = NotesConstants.PAGE_SIZE) @Positive @Max(50) int pageSize,

            @Parameter(in = ParameterIn.QUERY, name = "pso", description = "Sort Order", schema = @Schema(implementation = String.class))
            @RequestParam(name = "pso", required = false, defaultValue = NotesConstants.SORT_ORDER) String sortOrder,

            @Parameter(in = ParameterIn.QUERY, name = "psk", description = "Sort Key", schema = @Schema(implementation = String.class))
            @RequestParam(name = "psk", required = false, defaultValue = "updatedAt") String sortKey,

            @Parameter(in = ParameterIn.QUERY, name = "key", description = "Match Key", schema = @Schema(implementation = String.class))
            @RequestParam(name = "key", required = false, defaultValue = "") String keyword
    ) {
        UUID userId = authUtils.authenticatedUserId();
        PageResult<List<NoteDto>> result = notesService.fetchAllNotesForUser(
                userId, pageNumber, pageSize, sortOrder, sortKey, keyword
        );
        return ResponseEntity.ok(new NoteDtoCollectionResponse(result.getData(), result.getPageMeta()));
    }

    @Operation(
            summary = "Create Note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note Created Successfully", content = @Content(
                    schema = @Schema(implementation = NoteDtoApiResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access Denied", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @PostMapping
    public ResponseEntity<NoteDtoApiResponse> createNote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data To Create Note", required = true, content = @Content(schema = @Schema(implementation = CreateNoteRequest.class)))
            @Valid @RequestBody CreateNoteRequest createNoteRequest
    ) {

        UUID userId = authUtils.authenticatedUserId();
        NoteDto dto = notesService.createNote(userId, createNoteRequest);
        return new ResponseEntity<>(
                new NoteDtoApiResponse(
                        "Note Created Successfully",
                        dto
                ),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update Note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note Updated Successfully", content = @Content(
                    schema = @Schema(implementation = NoteDtoApiResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Invalid Data Or Parameters", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access Denied", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Note Not Found", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @PutMapping("/{noteId}")
    public ResponseEntity<NoteDtoApiResponse> updateNote(
            @Parameter(in = ParameterIn.PATH, required = true, name = "noteId", description = "Note Id", content = @Content(schema = @Schema(implementation = String.class)))
            @PathVariable String noteId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data To Update Note", required = true, content = @Content(schema = @Schema(implementation = PatchNoteRequest.class)))
            @Valid @RequestBody PatchNoteRequest patchNoteRequest
    ) {
        UUID userId = authUtils.authenticatedUserId();
        NoteDto dto = notesService.updateNote(userId, noteId, patchNoteRequest);
        return ResponseEntity.ok(
                new NoteDtoApiResponse(
                        "Note Updated Successfully",
                        dto
                )
        );
    }

    @Operation(
            summary = "Delete Note",
            description = "Delete Note By Id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Note Deleted Successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Data Or Parameters", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access Denied", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Note Not Found", content = @Content(
                    schema = @Schema(implementation = APIExceptionResponse.class)
            ))
    })
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNodeById(
            @Parameter(in = ParameterIn.PATH, required = true, name = "noteId", description = "Note Id", content = @Content(schema = @Schema(implementation = String.class)))
            @PathVariable String noteId
    ) {

        UUID userId = authUtils.authenticatedUserId();
        notesService.deleteNote(userId, noteId);
        return ResponseEntity.noContent().build();
    }
}
