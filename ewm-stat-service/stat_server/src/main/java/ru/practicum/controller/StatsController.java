package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/hit")
    public void saveStatHit(@RequestBody EndpointHitDto endpointHitDto) {
        statsRepository.save(endpointHitMapper.toEndpointHit(endpointHitDto));
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam(name = "start") String startString,
                                    @RequestParam(name = "end") String endString,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") boolean unique) {
        LocalDateTime start = LocalDateTime.parse(URLDecoder.decode(startString, StandardCharsets.UTF_8), formatter);
        LocalDateTime end = LocalDateTime.parse(URLDecoder.decode(endString, StandardCharsets.UTF_8), formatter);
        if (uris == null || uris.isEmpty()) {
            return statsRepository.getViewStats(start, end, unique);
        } else {
            return statsRepository.getViewStatsByUris(start, end, unique, uris);
        }
    }
}
