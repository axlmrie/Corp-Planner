package fr.axel.corpplanner.user;

import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.dto.UserResponse;
import fr.axel.corpplanner.user.dto.UserUpdateRequest;
import fr.axel.corpplanner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleName -> {
                        try {
                            return Role.valueOf(roleName);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Rôle " + roleName + " inexistant. Rôles valides : ADMIN, EMPLOYEE");
                        }
                    })
                    .collect(Collectors.toSet());

            user.setRoles(newRoles);
        }

        User userSaved = userRepository.save(user);
        return mapToResponse(userSaved);
    }

    public UserResponse mapToResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                roles,
                user.getFirstName(),
                user.getLastName()
        );
    }

    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapToResponse(user);
    }
}