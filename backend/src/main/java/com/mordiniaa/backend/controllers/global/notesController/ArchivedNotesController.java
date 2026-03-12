package com.mordiniaa.backend.controllers.global.notesController;

import com.mordiniaa.backend.config.NotesConstants;
import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.payload.CollectionResponse;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.notes.ArchivedNotesService;
import com.mordiniaa.backend.utils.PageResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notes/archive")
public class ArchivedNotesController {

    private final ArchivedNotesService archivedNotesService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<CollectionResponse<NoteDto>> getAllArchivedNotesForUser(
            @RequestParam(name = "pn", required = false, defaultValue = NotesConstants.PAGE_NUMBER) @PositiveOrZero int pageNumber,
            @RequestParam(name = "ps", required = false, defaultValue = NotesConstants.PAGE_SIZE) @Positive @Max(50) int pageSize
    ) {
        UUID userId = authUtils.authenticatedUserId();
        PageResult<List<NoteDto>> result = archivedNotesService.fetchAllArchivedNotes(userId, pageNumber, pageSize);
        return ResponseEntity.ok(new CollectionResponse<>(result.getData(), result.getPageMeta()));
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<Void> switchArchived(@PathVariable String noteId) {
        return null;
    }
}
