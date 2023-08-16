package ru.practicum.dto.party_request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.model.PartyRequest;

@Mapper(componentModel = "spring")
public interface PartyRequestMapper {
    @Mapping(source = "partyRequest.event.id", target = "event")
    @Mapping(source = "partyRequest.requester.id", target = "requester")
    PartyRequestDto toPartyRequestDto(PartyRequest partyRequest);
}
