package com.hangout.repository;

import com.hangout.model.Event;
import com.hangout.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);

    // Find events where user is organizer or has RSVP'd
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.rsvps r " +
           "WHERE e.organizer = :user OR r.guestEmail = :email " +
           "ORDER BY e.eventDate ASC")
    List<Event> findEventsByUserOrRsvp(@Param("user") User user, @Param("email") String email);

    // Find upcoming events where user is organizer or has RSVP'd
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.rsvps r " +
           "WHERE (e.organizer = :user OR r.guestEmail = :email) " +
           "AND e.eventDate > :currentDate " +
           "ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEventsByUserOrRsvp(@Param("user") User user,
                                                 @Param("email") String email,
                                                 @Param("currentDate") LocalDateTime currentDate);
}
