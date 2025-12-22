package com.hangout.controller;

import com.hangout.model.Driver;
import com.hangout.model.Rsvp;
import com.hangout.repository.DriverRepository;
import com.hangout.repository.EventRepository;
import com.hangout.repository.RsvpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RsvpRepository rsvpRepository;

    @GetMapping("/event/{eventId}")
    public List<DriverResponse> getDriversByEvent(@PathVariable Long eventId) {
        List<Driver> drivers = driverRepository.findByEventIdOrderByDepartureTimeAsc(eventId);
        return drivers.stream().map(driver -> {
            DriverResponse response = new DriverResponse();
            response.setId(driver.getId());
            response.setDriverName(driver.getDriverName());
            response.setDriverEmail(driver.getDriverEmail());
            response.setDepartureTime(driver.getDepartureTime());
            response.setCapacity(driver.getCapacity());
            response.setCarDetails(driver.getCarDetails());

            List<PassengerInfo> passengers = driver.getPassengers().stream()
                .map(rsvp -> {
                    PassengerInfo info = new PassengerInfo();
                    info.setName(rsvp.getGuestName());
                    info.setEmail(rsvp.getGuestEmail());
                    return info;
                })
                .collect(Collectors.toList());
            response.setPassengers(passengers);
            response.setSpotsAvailable(driver.getCapacity() - passengers.size());

            return response;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Driver> createDriver(@RequestBody DriverRequest request) {
        return eventRepository.findById(request.getEventId())
                .map(event -> {
                    Driver driver = new Driver();
                    driver.setEvent(event);
                    driver.setDriverName(request.getDriverName());
                    driver.setDriverEmail(request.getDriverEmail());
                    driver.setDepartureTime(request.getDepartureTime());
                    driver.setCapacity(request.getCapacity());
                    driver.setCarDetails(request.getCarDetails());
                    return ResponseEntity.ok(driverRepository.save(driver));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{driverId}/join")
    public ResponseEntity<Map<String, String>> joinCar(@PathVariable Long driverId, @RequestBody JoinCarRequest request) {
        return driverRepository.findById(driverId)
                .map(driver -> {
                    return rsvpRepository.findByEventIdAndGuestEmail(driver.getEvent().getId(), request.getGuestEmail())
                            .map(rsvp -> {
                                int currentPassengers = driver.getPassengers().size();
                                if (currentPassengers >= driver.getCapacity()) {
                                    Map<String, String> error = new HashMap<>();
                                    error.put("error", "Car is full");
                                    return ResponseEntity.badRequest().body(error);
                                }

                                rsvp.setDriver(driver);
                                rsvpRepository.save(rsvp);

                                Map<String, String> success = new HashMap<>();
                                success.put("message", "Successfully joined car");
                                return ResponseEntity.ok(success);
                            })
                            .orElseGet(() -> {
                                Map<String, String> error = new HashMap<>();
                                error.put("error", "Must RSVP yes before joining a car");
                                return ResponseEntity.badRequest().body(error);
                            });
                })
                .orElseGet(() -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Driver not found");
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/{driverId}/leave")
    public ResponseEntity<Map<String, String>> leaveCar(@PathVariable Long driverId, @RequestBody JoinCarRequest request) {
        return rsvpRepository.findByEventIdAndGuestEmail(
                driverRepository.findById(driverId).get().getEvent().getId(),
                request.getGuestEmail())
                .map(rsvp -> {
                    rsvp.setDriver(null);
                    rsvpRepository.save(rsvp);
                    Map<String, String> success = new HashMap<>();
                    success.put("message", "Left car successfully");
                    return ResponseEntity.ok(success);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class DriverRequest {
        private Long eventId;
        private String driverName;
        private String driverEmail;
        private LocalDateTime departureTime;
        private Integer capacity;
        private String carDetails;

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        public String getDriverName() { return driverName; }
        public void setDriverName(String driverName) { this.driverName = driverName; }
        public String getDriverEmail() { return driverEmail; }
        public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        public String getCarDetails() { return carDetails; }
        public void setCarDetails(String carDetails) { this.carDetails = carDetails; }
    }

    public static class JoinCarRequest {
        private String guestEmail;

        public String getGuestEmail() { return guestEmail; }
        public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    }

    public static class DriverResponse {
        private Long id;
        private String driverName;
        private String driverEmail;
        private LocalDateTime departureTime;
        private Integer capacity;
        private String carDetails;
        private List<PassengerInfo> passengers;
        private Integer spotsAvailable;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDriverName() { return driverName; }
        public void setDriverName(String driverName) { this.driverName = driverName; }
        public String getDriverEmail() { return driverEmail; }
        public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        public String getCarDetails() { return carDetails; }
        public void setCarDetails(String carDetails) { this.carDetails = carDetails; }
        public List<PassengerInfo> getPassengers() { return passengers; }
        public void setPassengers(List<PassengerInfo> passengers) { this.passengers = passengers; }
        public Integer getSpotsAvailable() { return spotsAvailable; }
        public void setSpotsAvailable(Integer spotsAvailable) { this.spotsAvailable = spotsAvailable; }
    }

    public static class PassengerInfo {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
