package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validator.annotation.NullableNotBlank;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationDto {
    @NullableNotBlank
    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned;

    private List<Long> events;
}
