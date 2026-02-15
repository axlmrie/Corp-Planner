package fr.axel.corpplanner.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.email.EmailService;
import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import fr.axel.corpplanner.resource.dto.ResourceRequest;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResourceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ResourceRepository resourceRepository;

    @MockitoBean private EmailService emailService;

    @Test
    @DisplayName("Tout le monde peut lister les ressources")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldListResources() throws Exception {
        mockMvc.perform(get("/api/v1/resources"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un ADMIN peut créer une ressource")
    @WithMockUser(roles = "ADMIN")
    void adminShouldCreateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Projecteur", ResourceType.HARDWARE, 1, "Bureau 202");

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Projecteur"));
    }

    @Test
    @DisplayName("Un EMPLOYÉ ne peut PAS créer une ressource (403)")
    @WithMockUser(roles = "EMPLOYEE")
    void employeeShouldNotCreateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Interdit", ResourceType.ROOM, 5, "Zone 51");

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("L'API devrait valider les champs obligatoires (400)")
    @WithMockUser(roles = "ADMIN")
    void shouldValidateBadRequest() throws Exception {
        ResourceRequest invalidRequest = new ResourceRequest("", null, 0, null);

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Devrait récupérer les détails d'une ressource par son ID")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldGetResourceDetail() throws Exception {
        Resource saved = resourceRepository.save(Resource.builder()
                .name("Salle de test")
                .type(ResourceType.ROOM)
                .active(true)
                .build());

        mockMvc.perform(get("/api/v1/resources/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salle de test"));
    }

    @Test
    @DisplayName("Un ADMIN peut modifier une ressource existante")
    @WithMockUser(roles = "ADMIN")
    void adminShouldUpdateResource() throws Exception {
        Resource saved = resourceRepository.save(Resource.builder()
                .name("Ancien Nom")
                .type(ResourceType.ROOM)
                .capacity(5)
                .active(true)
                .build());

        ResourceRequest updateRequest = new ResourceRequest("Nouveau Nom", ResourceType.ROOM, 10, "Etage 1");

        mockMvc.perform(put("/api/v1/resources/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nouveau Nom"))
                .andExpect(jsonPath("$.capacity").value(10));
    }

    @Test
    @DisplayName("Un ADMIN peut supprimer (désactiver) une ressource")
    @WithMockUser(roles = "ADMIN")
    void adminShouldDeleteResource() throws Exception {
        Resource saved = resourceRepository.save(Resource.builder()
                .name("A supprimer")
                .type(ResourceType.HARDWARE)
                .active(true)
                .build());

        mockMvc.perform(delete("/api/v1/resources/" + saved.getId()))
                .andExpect(status().isNoContent());

        Resource deleted = resourceRepository.findById(saved.getId()).orElseThrow();
        assert !deleted.getActive();
    }
}