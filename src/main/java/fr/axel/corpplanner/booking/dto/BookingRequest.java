package fr.axel.corpplanner.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingRequest (
        @NotNull(message = "La date de début est obligatoire")
        LocalDateTime startDate,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDateTime endDate,

        @NotNull(message = "Ressource obligatoire")
        Long resourceId
){}
