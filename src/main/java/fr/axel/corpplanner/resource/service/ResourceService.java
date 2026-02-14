package fr.axel.corpplanner.resource.service;

import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import fr.axel.corpplanner.resource.dto.ResourceRequest;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public Page<Resource> findAll(ResourceType type, Pageable pageable) {
        if (type != null) {
            return resourceRepository.findAllByTypeAndActiveTrue(type, pageable);
        }
        return resourceRepository.findAllByActiveTrue(pageable);
    }

    public Resource findById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + id));
    }

    public Resource create(ResourceRequest request) {
        Resource resource = Resource.builder()
                .name(request.name())
                .type(request.type())
                .capacity(request.capacity())
                .location(request.location())
                .active(true)
                .build();
        return resourceRepository.save(resource);
    }

    public Resource update(Long id, ResourceRequest request) {
        Resource existing = findById(id);

        existing.setName(request.name());
        existing.setType(request.type());
        existing.setCapacity(request.capacity());
        existing.setLocation(request.location());

        return resourceRepository.save(existing);
    }

    public void delete(Long id) {
        Resource resource = findById(id);
        resource.setActive(false);
        resourceRepository.save(resource);
    }
}
