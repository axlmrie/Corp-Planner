package fr.axel.corpplanner.user;


import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.dto.UserResponse;
import fr.axel.corpplanner.user.dto.UserUpdateRequest;
import fr.axel.corpplanner.user.repository.RoleRepository;
import fr.axel.corpplanner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Rôle " + roleName + " inexistant"));
                newRoles.add(role);
            }
            user.setRoles(newRoles);
        }

        User userSaved = userRepository.save(user);
        return mapToResponse(userSaved);
    }
    public UserResponse mapToResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::name)
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