package fr.axel.corpplanner.respirationExercice;

import fr.calip.script_manager.respirationExercice.domain.ParametreResipiration;
import fr.calip.script_manager.respirationExercice.repository.RespirationExerciceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - RespirationExerciceService")
class RespirationExerciceServiceTest {

    @Mock
    private RespirationExerciceRepository repository;

    @InjectMocks
    private RespirationExerciceService service;

    private ParametreResipiration sampleParam;

    @BeforeEach
    void setUp() {
        sampleParam = ParametreResipiration.builder()
                .id(1L)
                .nomPreset("Zen")
                .respiration(5)
                .apnee(2)
                .expiration(5)
                .build();
    }

    @Test
    @DisplayName("Should create and return a ParametreResipiration")
    void shouldCreateParametreResipiration() {
        // Given
        when(repository.save(any(ParametreResipiration.class))).thenReturn(sampleParam);

        // When
        ParametreResipiration result = service.createParametreResipiration(sampleParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNomPreset()).isEqualTo("Zen");
        verify(repository, times(1)).save(sampleParam);
    }

    @Test
    @DisplayName("Should return a list of all exercises")
    void shouldReturnAllExercises() {
        // Given
        when(repository.findAll()).thenReturn(List.of(sampleParam));

        // When
        List<ParametreResipiration> result = service.getAllParametreResipirations();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return exercise by ID when it exists")
    void shouldReturnExerciseById() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleParam));

        ParametreResipiration result = service.getParametreResipiration(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should call deleteById when deleting an exercise")
    void shouldDeleteExercise() {
        // When
        service.delete(1L);

        // Then
        verify(repository, times(1)).deleteById(1L);
    }
}