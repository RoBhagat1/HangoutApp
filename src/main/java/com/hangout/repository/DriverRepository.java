package com.hangout.repository;

import com.hangout.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByEventIdOrderByDepartureTimeAsc(Long eventId);
}
