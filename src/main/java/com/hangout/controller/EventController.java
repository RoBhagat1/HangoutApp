package com.hangout.controller;

import com.hangout.model.Event;
import com.hangout.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/upcoming")
    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        event.setCreatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        return eventRepository.findById(id)
                .map(event -> {
                    event.setTitle(eventDetails.getTitle());
                    event.setDescription(eventDetails.getDescription());
                    event.setEventDate(eventDetails.getEventDate());
                    event.setLocation(eventDetails.getLocation());
                    event.setOrganizerName(eventDetails.getOrganizerName());
                    event.setOrganizerEmail(eventDetails.getOrganizerEmail());
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(event -> {
                    eventRepository.delete(event);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
