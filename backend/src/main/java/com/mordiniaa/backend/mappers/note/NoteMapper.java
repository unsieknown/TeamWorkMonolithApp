package com.mordiniaa.backend.mappers.note;

import com.mordiniaa.backend.dto.note.NoteDto;
import com.mordiniaa.backend.exceptions.UnexpectedException;
import com.mordiniaa.backend.mappers.note.dtoMappers.AbstractNoteDtoMapper;
import com.mordiniaa.backend.mappers.note.modelMappers.AbstractNoteModelMapper;
import com.mordiniaa.backend.models.note.Note;
import com.mordiniaa.backend.request.note.NoteRequest;
import com.mordiniaa.backend.request.note.PatchNoteRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NoteMapper {

    private final Map<Class<? extends Note>, AbstractNoteDtoMapper<?, ?>> mapperByModelType;
    private final Map<Class<? extends NoteRequest>, AbstractNoteModelMapper<?, ?>> mapperByNoteRequestType;
    private final Map<Class<? extends Note>, AbstractNoteModelMapper<?, ?>> updateByNoteType;

    public NoteMapper(List<AbstractNoteDtoMapper<?, ?>> dtoMappers, List<AbstractNoteModelMapper<?, ?>> modelMappers) {
        this.mapperByModelType = dtoMappers.stream()
                .collect(Collectors.toMap(
                        AbstractNoteDtoMapper::getSupportedClass,
                        Function.identity()
                ));

        this.mapperByNoteRequestType = modelMappers.stream()
                .flatMap(mapper ->
                        mapper.getSupportedRequestClasses().stream()
                                .map(clazz -> Map.entry(clazz, mapper))
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        this.updateByNoteType = modelMappers.stream()
                .collect(Collectors.toMap(
                        AbstractNoteModelMapper::getSupportedClass,
                        Function.identity()
                ));
    }

    public NoteDto toDto(Note note) {

        AbstractNoteDtoMapper<?, ?> mapper = mapperByModelType.get(note.getClass());

        if (mapper == null) {
            throw new UnexpectedException("Unknow Error Occurred");
        }
        return mapper.toDto(note);
    }

    public Note toModel(NoteRequest noteRequest) {

        AbstractNoteModelMapper<?, ?> mapper = mapperByNoteRequestType.get(noteRequest.getClass());
        if (mapper == null) {
            throw new UnexpectedException("Unknow Error Occurred");
        }
        return mapper.toModel(noteRequest);
    }

    public void updateNote(Note note, PatchNoteRequest patchNoteRequest) {

        AbstractNoteModelMapper<?, ?> mapper = updateByNoteType.get(note.getClass());

        if (mapper == null) {
            throw new UnexpectedException("Unknow Error Occurred");
        }

        if (!mapper.getSupportedRequestClasses().contains(patchNoteRequest.getClass())) {
            throw new UnexpectedException("Unknow Error Occurred");
        }

        mapper.updateNote(note, patchNoteRequest);
    }
}
