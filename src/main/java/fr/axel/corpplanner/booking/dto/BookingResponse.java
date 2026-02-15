package fr.axel.corpplanner.booking.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        String resourceName
) {}