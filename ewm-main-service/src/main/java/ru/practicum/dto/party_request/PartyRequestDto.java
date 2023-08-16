package ru.practicum.dto.party_request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.PartyRequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRequestDto {
    private Long id;

    private Long event;

    private Long requester;

    private LocalDateTime created;

    private PartyRequestStatus status;
}
