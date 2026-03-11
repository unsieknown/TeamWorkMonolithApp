package com.mordiniaa.backend.services.notes;

import com.mordiniaa.backend.config.NotesConstants;
import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.mappers.note.NoteMapper;
import com.mordiniaa.backend.models.note.Note;
import com.mordiniaa.backend.repositories.mongo.NotesRepository;
import com.mordiniaa.backend.request.note.CreateNoteRequest;
import com.mordiniaa.backend.request.note.PatchNoteRequest;
import com.mordiniaa.backend.utils.PageResult;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotesService {

    private final NotesRepository notesRepository;
    private final NoteMapper noteMapper;
    private final MongoTemplate mongoTemplate;

    public Optional<NoteDto> getNoteById(String noteId, UUID ownerId) {

        if (!ObjectId.isValid(noteId)) {
            throw new IllegalArgumentException("Invalid Id");
        }

        Query query = new Query(
                Criteria.where("_id").is(new ObjectId(noteId))
                        .and("ownerId").is(ownerId)
        );

        return Optional.ofNullable(mongoTemplate.findOne(query, Note.class))
                .map(noteMapper::toDto);
    }

    public PageResult<List<NoteDto>> fetchAllNotesForUser(UUID ownerId, int pageNumber, int pageSize, String sortOrder, String sortKey, String keyword) {

        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            throw new RuntimeException();
        }

        if (!NotesConstants.ALLOWED_SORTING_KEYS.contains(sortKey)) {
            throw new RuntimeException(); //TODO: Change
        }

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortKey).ascending()
                : Sort.by(sortKey).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Note> page =
                (keyword != null && !keyword.isBlank())
                        ? notesRepository.findAllByOwnerIdAndArchivedFalse(
                        ownerId,
                        pageable,
                        TextCriteria.forDefaultLanguage().caseSensitive(false).matching(keyword))
                        : notesRepository.findAllByOwnerIdAndArchived(ownerId, false, pageable);

        PageResult<List<NoteDto>> result = new PageResult<>();

        result.setData(page.map(noteMapper::toDto).stream().toList());
        result.setUpPage(page);

        return result;
    }

    public NoteDto createNote(UUID ownerId, CreateNoteRequest createNoteRequest) {

        Note mappedNote = noteMapper.toModel(createNoteRequest);
        mappedNote.setOwnerId(ownerId);

        Note savedNote = notesRepository.save(mappedNote);
        return noteMapper.toDto(savedNote);
    }

    public NoteDto updateNote(UUID ownerId, String noteId, PatchNoteRequest patchNoteRequest) {

        if (!ObjectId.isValid(noteId)) {
            throw new RuntimeException();
        }

        ObjectId id = new ObjectId(noteId);
        Note note = notesRepository.findNoteByIdAndOwnerId(id, ownerId)
                .orElseThrow(RuntimeException::new); //TODO: Change in exceptions section
        noteMapper.updateNote(note, patchNoteRequest);

        Note savedNote = notesRepository.save(note);
        return noteMapper.toDto(savedNote);
    }

    public void deleteNote(UUID ownerId, String noteId) {

        if (!ObjectId.isValid(noteId)) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        long result = notesRepository.deleteByIdAndOwnerId(new ObjectId(noteId), ownerId);
        if (result != 1)
            throw new RuntimeException(); // TODO: Change to NotFound in Exceptions Section
    }
}
