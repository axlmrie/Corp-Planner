package fr.axel.corpplanner.user.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        Set<String> roles,
        Set<String> permissions,
        String picture,
        String company,
        String firstName,
        String lastName,
        String jobTitle
) {}