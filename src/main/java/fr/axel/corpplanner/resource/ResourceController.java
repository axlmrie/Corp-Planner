package fr.axel.corpplanner.resource;

import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import fr.axel.corpplanner.resource.dto.ResourceRequest;
import fr.axel.corpplanner.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Ressources", description = "Gestion du parc matériel (Salles, Véhicules...)")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @Operation(summary = "Lister les ressources", description = "Récupère les ressources actives avec pagination.")
    @ApiResponse(responseCode = "200", description = "Succès")
    public ResponseEntity<Page<Resource>> getAllResources(
            @RequestParam(required = false) ResourceType type,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(resourceService.findAll(type, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une ressource", description = "Récupère les details d'une ressources actives.")
    @ApiResponse(responseCode = "200", description = "Succès")
    public ResponseEntity<Resource> getResource(@PathVariable long id) {
        return ResponseEntity.ok(resourceService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une ressource", description = "Nécessite le rôle ADMIN.")
    @ApiResponse(responseCode = "201", description = "Ressource créée")
    @ApiResponse(responseCode = "403", description = "Interdit aux employés")
    public ResponseEntity<Resource> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier une ressource", description = "Nécessite le rôle ADMIN.")
    @ApiResponse(responseCode = "201", description = "Ressource créée")
    @ApiResponse(responseCode = "403", description = "Interdit aux employés")
    public ResponseEntity<Resource> updateResource(@PathVariable long id, @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une ressource", description = "Nécessite le rôle ADMIN.")
    @ApiResponse(responseCode = "201", description = "Ressource créée")
    @ApiResponse(responseCode = "403", description = "Interdit aux employés")
    public ResponseEntity<Void> deleteResource(@PathVariable long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}