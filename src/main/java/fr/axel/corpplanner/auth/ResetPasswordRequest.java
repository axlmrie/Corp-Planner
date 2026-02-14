package fr.axel.corpplanner.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String email,
        @NotBlank String code,
        @NotBlank @Size(min = 8) String password
) {}