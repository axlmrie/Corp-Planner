package fr.axel.corpplanner.user.repository;

import fr.axel.corpplanner.user.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}