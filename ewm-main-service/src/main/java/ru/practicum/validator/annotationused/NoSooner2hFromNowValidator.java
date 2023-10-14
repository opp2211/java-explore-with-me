package ru.practicum.validator.annotationused;

import ru.practicum.validator.annotation.NoSooner2hFromNow;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class NoSooner2hFromNowValidator implements ConstraintValidator<NoSooner2hFromNow, LocalDateTime> {
    @Override
    public boolean isValid(LocalDateTime dt, ConstraintValidatorContext constraintValidatorContext) {
        return dt.isAfter(LocalDateTime.now().plusHours(2));
    }
}