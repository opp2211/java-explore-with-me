package ru.practicum.repository.custom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {
    private final EntityManager em;
    @Override
    public List<Event> getAllFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> event = cq.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        if (userIds != null && !userIds.isEmpty()) {
            predicates.add(event.get("user_id").in(userIds));
        }
        if (strStates != null && !strStates.isEmpty()) {
            predicates.add(event.get("state").in(strStates));
        }
        if (catIds != null && !catIds.isEmpty()) {
            predicates.add(event.get("category_id").in(catIds));
        }
        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("event_date"), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("event_date"), rangeEnd));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        TypedQuery<Event> typedQuery = em.createQuery(cq);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }
}