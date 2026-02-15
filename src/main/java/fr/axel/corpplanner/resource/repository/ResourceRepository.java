package fr.axel.corpplanner.resource.repository;

import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Page<Resource> findAllByTypeAndActiveTrue(ResourceType type, Pageable pageable);

    Page<Resource> findAllByActiveTrue(Pageable pageable);
}
