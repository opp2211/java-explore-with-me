package ru.practicum.dto.party_request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRequestStatusUpdateResult {
    private List<PartyRequestDto> confirmedRequests = new ArrayList<>();
    private List<PartyRequestDto> rejectedRequests = new ArrayList<>();
}
