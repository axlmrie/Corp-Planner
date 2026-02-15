package fr.axel.corpplanner.resource;

import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import fr.axel.corpplanner.resource.dto.ResourceRequest;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import fr.axel.corpplanner.resource.service.ResourceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock private ResourceRepository resourceRepository;
    @InjectMocks private ResourceService resourceService;

    @Test
    @DisplayName("Devrait créer une ressource avec succès")
    void shouldCreateResource() {
        ResourceRequest request = new ResourceRequest("Salle A", ResourceType.ROOM, 10, "Paris");
        when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

        Resource result = resourceService.create(request);

        assertThat(result.getName()).isEqualTo("Salle A");
        assertThat(result.getActive()).isTrue();
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("Devrait trouver une ressource par son ID")
    void shouldFindById() {
        Resource resource = Resource.builder().id(1L).name("Test").build();
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        Resource result = resourceService.findById(1L);

        assertThat(result.getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Devrait lancer une exception si la ressource n'existe pas")
    void shouldThrowWhenNotFound() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> resourceService.findById(99L));
    }

    @Test
    @DisplayName("Devrait lister toutes les ressources actives avec pagination")
    void shouldFindAllActive() {
        Pageable pageable = Pageable.unpaged();
        Page<Resource> page = new PageImpl<>(List.of(new Resource()));
        when(resourceRepository.findAllByActiveTrue(pageable)).thenReturn(page);

        Page<Resource> result = resourceService.findAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait lister les ressources par type")
    void shouldFindByType() {
        Pageable pageable = Pageable.unpaged();
        when(resourceRepository.findAllByTypeAndActiveTrue(eq(ResourceType.ROOM), any())).thenReturn(Page.empty());

        resourceService.findAll(ResourceType.ROOM, pageable);

        verify(resourceRepository).findAllByTypeAndActiveTrue(ResourceType.ROOM, pageable);
    }

    @Test
    @DisplayName("Devrait désactiver une ressource au lieu de la supprimer (Soft Delete)")
    void shouldSoftDeleteResource() {
        Resource resource = Resource.builder().id(1L).active(true).build();
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        resourceService.delete(1L);

        assertThat(resource.getActive()).isFalse();
        verify(resourceRepository).save(resource);
    }
}