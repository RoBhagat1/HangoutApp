package com.hangout.controller;

import com.hangout.model.Event;
import com.hangout.model.Rsvp;
import com.hangout.repository.EventRepository;
import com.hangout.repository.RsvpRepository;
import com.hangout.service.CarpoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rsvps")
@CrossOrigin(origins = "*")
public class RsvpController {

    @Autowired
    private RsvpRepository rsvpRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CarpoolService carpoolService;

    @GetMapping("/event/{eventId}")
    public List<Rsvp> getRsvpsByEvent(@PathVariable Long eventId) {
        return rsvpRepository.findByEventId(eventId);
    }

    @PostMapping
    public ResponseEntity<Rsvp> createRsvp(@RequestBody RsvpRequest request) {
        return eventRepository.findById(request.getEventId())
                .map(event -> {
                    Rsvp existingRsvp = rsvpRepository
                            .findByEventIdAndGuestEmail(request.getEventId(), request.getGuestEmail())
                            .orElse(null);

                    Rsvp rsvp;
                    if (existingRsvp != null) {
                        existingRsvp.setStatus(request.getStatus());
                        existingRsvp.setMessage(request.getMessage());
                        existingRsvp.setIsDriver(request.getIsDriver());
                        existingRsvp.setArrivalTime(request.getArrivalTime());
                        existingRsvp.setSeats(request.getSeats());
                        existingRsvp.setRespondedAt(LocalDateTime.now());
                        rsvp = rsvpRepository.save(existingRsvp);
                    } else {
                        rsvp = new Rsvp();
                        rsvp.setEvent(event);
                        rsvp.setGuestName(request.getGuestName());
                        rsvp.setGuestEmail(request.getGuestEmail());
                        rsvp.setStatus(request.getStatus());
                        rsvp.setMessage(request.getMessage());
                        rsvp.setIsDriver(request.getIsDriver());
                        rsvp.setArrivalTime(request.getArrivalTime());
                        rsvp.setSeats(request.getSeats());
                        rsvp = rsvpRepository.save(rsvp);
                    }

                    // Auto-assign carpools if event is in AUTO mode
                    if (event.getCarpoolMode() == Event.CarpoolMode.AUTO) {
                        carpoolService.autoAssignCarpools(event);
                    }

                    return ResponseEntity.ok(rsvp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class RsvpRequest {
        private Long eventId;
        private String guestName;
        private String guestEmail;
        private Rsvp.RsvpStatus status;
        private String message;
        private Boolean isDriver;
        private LocalDateTime arrivalTime;
        private Integer seats;

        public Long getEventId() {
            return eventId;
        }

        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }

        public String getGuestName() {
            return guestName;
        }

        public void setGuestName(String guestName) {
            this.guestName = guestName;
        }

        public String getGuestEmail() {
            return guestEmail;
        }

        public void setGuestEmail(String guestEmail) {
            this.guestEmail = guestEmail;
        }

        public Rsvp.RsvpStatus getStatus() {
            return status;
        }

        public void setStatus(Rsvp.RsvpStatus status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getIsDriver() {
            return isDriver;
        }

        public void setIsDriver(Boolean isDriver) {
            this.isDriver = isDriver;
        }

        public LocalDateTime getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(LocalDateTime arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public Integer getSeats() {
            return seats;
        }

        public void setSeats(Integer seats) {
            this.seats = seats;
        }
    }
}
