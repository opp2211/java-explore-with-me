package ru.practicum.repository.custom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

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
    public List<Event> getAllAdminFiltered(List<Long> userIds, List<EventState> states, List<Long> catIds,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> event = cq.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        if (userIds != null && !userIds.isEmpty()) {
            predicates.add(event.get("initiator").get("id").in(userIds));
        }
        if (states != null && !states.isEmpty()) {
            predicates.add(event.get("state").in(states));
        }
        if (catIds != null && !catIds.isEmpty()) {
            predicates.add(event.get("category").get("id").in(catIds));
        }
        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("eventDate"), rangeEnd));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        TypedQuery<Event> typedQuery = em.createQuery(cq);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }

    @Override
    public List<Event> getAllPublicFiltered(String text, List<Long> catIds, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> event = cq.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(event.get("state"), EventState.PUBLISHED));

        if (text != null) {
            Predicate annotationLike = cb.like(cb.upper(event.get("annotation")), "%" + text.toUpperCase() + "%");
            Predicate descriptionLike = cb.like(cb.upper(event.get("description")), "%" + text.toUpperCase() + "%");
            predicates.add(cb.or(annotationLike, descriptionLike));
        }
        if (catIds != null && !catIds.isEmpty()) {
            predicates.add(event.get("category").get("id").in(catIds));
        }
        if (paid != null) {
            predicates.add(cb.equal(event.get("paid"), paid));
        }
        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("eventDate"), rangeEnd));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(event.get("eventDate")));
        TypedQuery<Event> typedQuery = em.createQuery(cq);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }
}
