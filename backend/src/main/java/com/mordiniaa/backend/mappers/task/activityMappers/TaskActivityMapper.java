package com.mordiniaa.backend.mappers.task.activityMappers;

import com.mordiniaa.backend.dto.task.activity.TaskActivityElementDto;
import com.mordiniaa.backend.exceptions.UnexpectedException;
import com.mordiniaa.backend.mappers.task.activityMappers.dtoMappers.AbstractActivityDtoMapper;
import com.mordiniaa.backend.models.task.activity.TaskActivityElement;
import com.mordiniaa.backend.models.user.mongodb.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TaskActivityMapper {

    private final Map<Class<? extends TaskActivityElement>, AbstractActivityDtoMapper<?, ?>> taskActivityDtoMappers;

    public TaskActivityMapper(List<AbstractActivityDtoMapper<?, ?>> dtoMappers) {
        this.taskActivityDtoMappers = dtoMappers.stream()
                .collect(Collectors.toMap(
                        AbstractActivityDtoMapper::getSupportedType,
                        Function.identity()
                ));
    }

    public TaskActivityElementDto toDto(TaskActivityElement element, UserRepresentation user) {

        AbstractActivityDtoMapper<?, ?> mapper = taskActivityDtoMappers.get(element.getClass());
        if (mapper == null) {
            throw new UnexpectedException("Unknow Error Occurred");
        }
        return mapper.toDto(element, user);
    }
}
