package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validator.NullableNotBlank;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventDto {
    @NullableNotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NullableNotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NullableNotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Positive
    private Long category;

    private Boolean paid;

    private Long participantLimit;

    private Boolean requestModeration;

    private Location location;

    private EventUpdateAction stateAction;
}
