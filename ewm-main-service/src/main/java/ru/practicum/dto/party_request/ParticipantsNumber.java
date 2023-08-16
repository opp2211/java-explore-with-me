package ru.practicum.dto.party_request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipantsNumber {
    private Long eventId;
    private Long number;
}
