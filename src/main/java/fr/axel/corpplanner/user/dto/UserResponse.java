package fr.axel.corpplanner.user.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        Set<String> roles,
        String firstName,
        String lastName
) {}