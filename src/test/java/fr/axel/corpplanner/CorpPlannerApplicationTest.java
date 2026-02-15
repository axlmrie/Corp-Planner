package fr.axel.corpplanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class CorpPlannerApplicationTest {

    @Test
    @DisplayName("La méthode main devrait démarrer l'application Spring Boot")
    void mainMethodShouldStartSpringApplication() {
        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {

            springApplicationMock.when(() -> SpringApplication.run(eq(CorpPlannerApplication.class), any(String[].class)))
                    .thenReturn(null);

            CorpPlannerApplication.main(new String[]{});

            springApplicationMock.verify(() -> SpringApplication.run(eq(CorpPlannerApplication.class), any(String[].class)));
        }
    }
}