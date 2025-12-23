package com.hangout.controller;

import com.hangout.model.Event;
import com.hangout.model.User;
import com.hangout.repository.EventRepository;
import com.hangout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Event> getAllEvents(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Must be logged in to view events");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return eventRepository.findEventsByUserOrRsvp(user, user.getEmail());
    }

    @GetMapping("/upcoming")
    public List<Event> getUpcomingEvents(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Must be logged in to view events");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return eventRepository.findUpcomingEventsByUserOrRsvp(user, user.getEmail(), LocalDateTime.now());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Must be logged in to create events");
        }

        User organizer = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        event.setOrganizer(organizer);
        event.setCreatedAt(LocalDateTime.now());

        // Backward compatibility: also set string fields
        event.setOrganizerName(organizer.getName());
        event.setOrganizerEmail(organizer.getEmail());

        return eventRepository.save(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                             @RequestBody Event eventDetails,
                                             Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return eventRepository.findById(id)
                .map(event -> {
                    // Check if user is the organizer
                    if (event.getOrganizer() != null &&
                        !event.getOrganizer().getEmail().equals(principal.getName())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Only event organizer can update");
                    }

                    event.setTitle(eventDetails.getTitle());
                    event.setDescription(eventDetails.getDescription());
                    event.setEventDate(eventDetails.getEventDate());
                    event.setLocation(eventDetails.getLocation());
                    event.setCarpoolMode(eventDetails.getCarpoolMode());

                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return eventRepository.findById(id)
                .map(event -> {
                    // Check if user is the organizer
                    if (event.getOrganizer() != null &&
                        !event.getOrganizer().getEmail().equals(principal.getName())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Only event organizer can delete");
                    }

                    eventRepository.delete(event);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
