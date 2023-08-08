package ru.practicum.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.dto.ViewStats(h.app, h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h) END) " +
            "FROM EndpointHit h " +
            "WHERE h.hitDatetime >= :start " +
            "AND h.hitDatetime <= :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(*) DESC")
    List<ViewStats> getViewStats(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("unique") boolean unique);

    @Query("SELECT new ru.practicum.dto.ViewStats(h.app, h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h) END) " +
            "FROM EndpointHit h " +
            "WHERE h.hitDatetime >= :start " +
            "AND h.hitDatetime <= :end " +
            "AND h.uri IN :uris " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(*) DESC")
    List<ViewStats> getViewStatsByUris(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("unique") boolean unique,
                                       @Param("uris") List<String> uris);
}
