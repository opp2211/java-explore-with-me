package ru.practicum.validator.annotation;

import ru.practicum.validator.annotationused.NoSooner2hFromNowValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NoSooner2hFromNowValidator.class)
public @interface NoSooner2hFromNow {
    String message() default "{javax.validation.constraints.NoSooner2hFromNow.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}