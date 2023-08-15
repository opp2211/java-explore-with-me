package ru.practicum.statsclient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsClient {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate restTemplate;

    public StatsClient(String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void saveStatHit(EndpointHitDto endpointHitDto) {
        restTemplate.postForEntity("/hit", buildHttpEntity(endpointHitDto), EndpointHitDto.class);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, boolean unique, @Nullable List<String> uris) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8));
        parameters.put("end", URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8));
        parameters.put("unique", unique);
        String url = "/stats?start={start}&end={end}&unique={unique}";

        if (uris != null) {
            url = url + "&uris={uris}";
            parameters.put("uris", uris);
        }

        ResponseEntity<List<ViewStats>> responseEntity = restTemplate.exchange(url,
                HttpMethod.GET,
                buildHttpEntity(null),
                new ParameterizedTypeReference<List<ViewStats>>() {},
                parameters);

        return responseEntity.getBody();
    }

    private <T> HttpEntity<T> buildHttpEntity(@Nullable T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(body, headers);
    }
}
