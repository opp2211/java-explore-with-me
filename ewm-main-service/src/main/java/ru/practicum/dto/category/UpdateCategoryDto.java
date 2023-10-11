package ru.practicum.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validator.annotation.NullableNotBlank;

import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryDto {
    @NullableNotBlank
    @Size(min = 1, max = 50)
    private String name;
}
