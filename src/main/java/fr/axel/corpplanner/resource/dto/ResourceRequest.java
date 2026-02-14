package fr.axel.corpplanner.resource.dto;

import fr.axel.corpplanner.resource.domain.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResourceRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String name,

        @NotNull(message = "Le type est obligatoire")
        ResourceType type,

        @Min(value = 1, message = "La capacité minimum est 1")
        Integer capacity,

        String location
) {}