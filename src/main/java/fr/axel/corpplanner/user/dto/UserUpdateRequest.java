package fr.axel.corpplanner.user.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    private String email;
    private Set<String> roles;
    private Set<String> companies;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String companyName;
}