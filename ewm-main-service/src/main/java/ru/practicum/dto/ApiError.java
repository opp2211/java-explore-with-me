package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiError {
    HttpStatus status;
    String reason;
    String message;
    String timestamp;
}
