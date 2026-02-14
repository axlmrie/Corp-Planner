package fr.axel.corpplanner.user;

import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.dto.UserResponse;
import fr.axel.corpplanner.user.dto.UserUpdateRequest;
import fr.axel.corpplanner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

//    @PreAuthorize("hasRole('ADMIN')")


    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody UserUpdateRequest request
    ){
        User currentUser = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Session invalide : utilisateur non trouvé"));

        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getByEmail(principal.getUsername()));
    }


}