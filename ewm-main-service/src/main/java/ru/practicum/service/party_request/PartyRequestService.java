package ru.practicum.service.party_request;

import ru.practicum.dto.party_request.PartyRequestDto;

import java.util.List;

public interface PartyRequestService {
    PartyRequestDto addNew(long userId, long eventId);
    List<PartyRequestDto> getAllByUserId(long userId);
    void cancelOwn(long userId, long requestId);
}
