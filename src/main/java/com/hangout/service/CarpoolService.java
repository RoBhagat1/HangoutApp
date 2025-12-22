package com.hangout.service;

import com.hangout.model.Driver;
import com.hangout.model.Event;
import com.hangout.model.Rsvp;
import com.hangout.repository.DriverRepository;
import com.hangout.repository.RsvpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarpoolService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RsvpRepository rsvpRepository;

    public void autoAssignCarpools(Event event) {
        List<Rsvp> attendees = rsvpRepository.findByEventId(event.getId()).stream()
                .filter(rsvp -> rsvp.getStatus() == Rsvp.RsvpStatus.YES)
                .filter(rsvp -> rsvp.getArrivalTime() != null)
                .sorted(Comparator.comparing(Rsvp::getArrivalTime))
                .collect(Collectors.toList());

        // Clear existing driver assignments
        attendees.forEach(rsvp -> rsvp.setDriver(null));

        // Separate drivers and passengers
        List<Rsvp> drivers = attendees.stream()
                .filter(rsvp -> Boolean.TRUE.equals(rsvp.getIsDriver()))
                .collect(Collectors.toList());

        List<Rsvp> passengers = attendees.stream()
                .filter(rsvp -> !Boolean.TRUE.equals(rsvp.getIsDriver()))
                .collect(Collectors.toList());

        // Delete old auto-created drivers for this event
        List<Driver> existingDrivers = driverRepository.findByEventIdOrderByDepartureTimeAsc(event.getId());
        driverRepository.deleteAll(existingDrivers);

        // Create Driver entities for each driver RSVP
        for (Rsvp driverRsvp : drivers) {
            Driver driver = new Driver();
            driver.setEvent(event);
            driver.setDriverName(driverRsvp.getGuestName());
            driver.setDriverEmail(driverRsvp.getGuestEmail());
            driver.setDepartureTime(driverRsvp.getArrivalTime());
            driver.setCapacity(driverRsvp.getSeats() != null ? driverRsvp.getSeats() : 4);
            driver.setCarDetails("Auto-assigned");
            driver = driverRepository.save(driver);

            // Driver takes their own spot
            driverRsvp.setDriver(driver);
            rsvpRepository.save(driverRsvp);
        }

        // Assign passengers to drivers based on similar arrival times
        List<Driver> savedDrivers = driverRepository.findByEventIdOrderByDepartureTimeAsc(event.getId());

        for (Rsvp passenger : passengers) {
            Driver bestDriver = findBestDriver(passenger, savedDrivers);
            if (bestDriver != null) {
                passenger.setDriver(bestDriver);
                rsvpRepository.save(passenger);
            }
        }
    }

    private Driver findBestDriver(Rsvp passenger, List<Driver> drivers) {
        Driver bestMatch = null;
        long smallestTimeDiff = Long.MAX_VALUE;

        for (Driver driver : drivers) {
            // Check if driver has available seats
            long currentPassengers = driver.getPassengers().size();
            if (currentPassengers >= driver.getCapacity()) {
                continue;
            }

            // Find driver with closest departure time
            long timeDiff = Math.abs(
                java.time.Duration.between(passenger.getArrivalTime(), driver.getDepartureTime()).toMinutes()
            );

            if (timeDiff < smallestTimeDiff) {
                smallestTimeDiff = timeDiff;
                bestMatch = driver;
            }
        }

        return bestMatch;
    }
}
