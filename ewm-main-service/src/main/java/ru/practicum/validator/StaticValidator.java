package ru.practicum.validator;

import ru.practicum.exception.ValidationException;

public class StaticValidator {
    public static void validateFromSize(int from, int size) {
        if (from % size != 0) {
            throw new ValidationException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
    }
}
