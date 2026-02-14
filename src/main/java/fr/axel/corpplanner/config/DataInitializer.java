package fr.axel.corpplanner.config;

import fr.axel.corpplanner.user.domain.Permission;
import fr.axel.corpplanner.user.repository.PermissionRepository;
import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("USER").isEmpty()) {
            Permission readPermission = Permission.builder().name("CAN_READ").build();
            permissionRepository.save(readPermission);

            Role userRole = Role.builder()
                    .name("USER")
                    .permissions(Set.of(readPermission))
                    .build();
            roleRepository.save(userRole);

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .build();
            roleRepository.save(adminRole);
        }
    }
}