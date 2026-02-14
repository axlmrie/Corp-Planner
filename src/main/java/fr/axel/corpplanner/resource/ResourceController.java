package fr.axel.corpplanner.resource;

import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import fr.axel.corpplanner.resource.dto.ResourceRequest;
import fr.axel.corpplanner.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<Page<Resource>> getAllResources(
            @RequestParam(required = false) ResourceType type,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(resourceService.findAll(type, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResource(@PathVariable long id) {
        return ResponseEntity.ok(resourceService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(request));
    }

    // PUT : On utilise le DTO ResourceRequest
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> updateResource(@PathVariable long id, @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}