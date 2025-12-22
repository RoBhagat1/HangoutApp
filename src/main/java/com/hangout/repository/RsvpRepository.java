package com.hangout.repository;

import com.hangout.model.Rsvp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RsvpRepository extends JpaRepository<Rsvp, Long> {
    List<Rsvp> findByEventId(Long eventId);
    Optional<Rsvp> findByEventIdAndGuestEmail(Long eventId, String guestEmail);
}
