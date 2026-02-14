package fr.axel.corpplanner.booking.repository;

import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
        SELECT COUNT(b) > 0 
        FROM Booking b 
        WHERE b.resource.id = :resourceId 
        AND b.status != 'CANCELLED' 
        AND (b.startDate < :endDate AND b.endDate > :startDate)
    """)
    boolean hasConflictingBooking(
            @Param("resourceId") Long resourceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );}
