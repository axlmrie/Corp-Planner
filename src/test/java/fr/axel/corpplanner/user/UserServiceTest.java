package fr.axel.corpplanner.user;

import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.dto.UserResponse;
import fr.axel.corpplanner.user.dto.UserUpdateRequest;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("UpdateUser : Mise à jour partielle réussie")
    void updateUserSuccess() {
        User existingUser = User.builder().id(1L).email("old@test.com").roles(new HashSet<>()).build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("NouveauNom");
        request.setRoles(Set.of("ADMIN"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.firstName()).isEqualTo("NouveauNom");
        assertThat(response.roles()).contains("ADMIN");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("UpdateUser : Doit lancer une erreur si l'user n'existe pas")
    void updateUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUser(99L, new UserUpdateRequest()));
    }

    @Test
    @DisplayName("UpdateUser : Doit lancer une erreur si le rôle est invalide")
    void updateUserInvalidRole() {
        User existingUser = User.builder().id(1L).roles(new HashSet<>()).build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setRoles(Set.of("ROLE_INEXISTANT"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("GetByEmail : Doit retourner une réponse si trouvé")
    void getByEmailSuccess() {
        User user = User.builder().email("test@test.com").roles(new HashSet<>()).build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.getByEmail("test@test.com");

        assertThat(response.email()).isEqualTo("test@test.com");
    }
}