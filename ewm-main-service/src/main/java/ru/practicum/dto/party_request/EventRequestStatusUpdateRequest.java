package ru.practicum.dto.party_request;

import lombok.Data;
import ru.practicum.model.PartyRequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private PartyRequestStatus status;
}
