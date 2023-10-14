package ru.practicum.controller.privateapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.party_request.PartyRequestDto;
import ru.practicum.service.party_request.PartyRequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class PartyRequestControllerPrivate {
    private final PartyRequestService partyRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartyRequestDto addNew(@Positive @PathVariable long userId,
                                  @Positive @RequestParam long eventId) {
        return partyRequestService.addNew(userId, eventId);
    }

    @GetMapping
    public List<PartyRequestDto> getAllByUserId(@Positive @PathVariable long userId) {
        return partyRequestService.getAllByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public PartyRequestDto cancelOwn(@Positive @PathVariable long userId,
                                     @Positive @PathVariable long requestId) {
        return partyRequestService.cancelOwn(userId, requestId);
    }
}
