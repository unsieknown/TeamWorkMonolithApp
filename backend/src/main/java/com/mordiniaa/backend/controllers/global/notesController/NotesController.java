package com.mordiniaa.backend.controllers.global.notesController;

import com.mordiniaa.backend.config.NotesConstants;
import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.request.note.CreateNoteRequest;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.notes.NotesService;
import com.mordiniaa.backend.utils.PageResult;
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

@Validated
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NotesController {

    private final NotesService notesService;
    private final AuthUtils authUtils;

    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteDto>> getNoteById(@PathVariable String noteId) {

        UUID userId = authUtils.authenticatedUserId();
        return notesService.getNoteById(
                        noteId,
                        userId
                )
                .map(dto -> ResponseEntity.ok(new ApiResponse<>("Success", dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionResponse<NoteDto>> fetchAllNotesForUser(
            @RequestParam(name = "pn", required = false, defaultValue = NotesConstants.PAGE_NUMBER) @PositiveOrZero int pageNumber,
            @RequestParam(name = "ps", required = false, defaultValue = NotesConstants.PAGE_SIZE) @Positive @Max(0) int pageSize,
            @RequestParam(name = "pso", required = false, defaultValue = NotesConstants.SORT_ORDER) String sortOrder,
            @RequestParam(name = "psk", required = false, defaultValue = "updatedAt") String sortKey,
            @RequestParam(name = "key", required = false, defaultValue = "") String keyword
    ) {
        PageResult<List<NoteDto>> result = notesService.fetchAllNotesForUser(UUID.randomUUID(), //TODO: Get id from user in security section
                pageNumber, pageSize, sortOrder, sortKey, keyword);
        return ResponseEntity.ok(new CollectionResponse<>(result.getData(), result.getPageMeta()));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<CollectionResponse<NoteDto>> getAutocompleteSuggestions(
            @RequestParam(name = "pn", required = false, defaultValue = NotesConstants.PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "key", required = false, defaultValue = "") String keyword
    ) {
        return null;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteDto>> createNote(
            @Valid
            @RequestBody
            CreateNoteRequest createNoteRequest
    ) {

        UUID userId = authUtils.authenticatedUserId();
        NoteDto dto = notesService.createNote(userId, createNoteRequest);
        return new ResponseEntity<>(
                new ApiResponse<>(
                        "Note Created Successfully",
                        dto
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteDto>> updateNote(@PathVariable String noteId, @RequestBody NoteDto noteDto) {
        return null;
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNodeById(@PathVariable String noteId) {
        return null;
    }
}
