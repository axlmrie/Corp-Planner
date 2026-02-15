package fr.axel.corpplanner.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.booking.domain.Status;
import fr.axel.corpplanner.booking.dto.BookingRequest;
import fr.axel.corpplanner.booking.repository.BookingRepository;
import fr.axel.corpplanner.email.EmailService;
import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.domain.ResourceType;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    private User user;
    private User admin;
    private Resource meetingRoom;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("employee@corp.com")
                .password("pass")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(Role.EMPLOYEE))
                .enabled(true)
                .build());

        admin = userRepository.save(User.builder()
                .email("admin@corp.com")
                .password("pass")
                .firstName("Admin")
                .lastName("Boss")
                .roles(Set.of(Role.ADMIN))
                .enabled(true)
                .build());

        meetingRoom = resourceRepository.save(Resource.builder()
                .name("Salle A")
                .type(ResourceType.ROOM)
                .capacity(10)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("Devrait créer une réservation avec succès (201)")
    @WithMockUser(username = "employee@corp.com")
    void shouldCreateBooking() throws Exception {
        BookingRequest request = new BookingRequest(
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
                meetingRoom.getId()
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.resourceName").value("Salle A"));
    }

    @Test
    @DisplayName("Devrait refuser une réservation en conflit de date (409)")
    @WithMockUser(username = "employee@corp.com")
    void shouldRejectConflictingBooking() throws Exception {
        bookingRepository.save(Booking.builder()
                .startDate(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endDate(LocalDateTime.now().plusDays(1).withHour(12).withMinute(0))
                .resource(meetingRoom)
                .user(user)
                .status(Status.CONFIRMED)
                .build());

        BookingRequest conflictRequest = new BookingRequest(
                LocalDateTime.now().plusDays(1).withHour(11).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(13).withMinute(0),
                meetingRoom.getId()
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflit de réservation"));
    }

    @Test
    @DisplayName("Devrait permettre au propriétaire d'annuler sa réservation")
    @WithMockUser(username = "employee@corp.com")
    void shouldCancelOwnBooking() throws Exception {
        Booking booking = bookingRepository.save(Booking.builder()
                .startDate(LocalDateTime.now().plusDays(2))
                .endDate(LocalDateTime.now().plusDays(2).plusHours(1))
                .resource(meetingRoom)
                .user(user)
                .status(Status.CONFIRMED)
                .build());

        mockMvc.perform(delete("/api/v1/bookings/" + booking.getId()))
                .andExpect(status().isNoContent()); // 204

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assert updatedBooking.getStatus() == Status.CANCELLED;
    }

    @Test
    @DisplayName("Devrait interdire à un autre employé d'annuler (403)")
    @WithMockUser(username = "other@corp.com")
    void shouldForbidCancellationByOther() throws Exception {
        userRepository.save(User.builder().email("other@corp.com").password("pwd").roles(Set.of(Role.EMPLOYEE)).build());

        Booking booking = bookingRepository.save(Booking.builder()
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(3).plusHours(1))
                .resource(meetingRoom)
                .user(user)
                .status(Status.CONFIRMED)
                .build());

        mockMvc.perform(delete("/api/v1/bookings/" + booking.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait permettre à un ADMIN d'annuler n'importe quelle réservation")
    @WithMockUser(username = "admin@corp.com", roles = {"ADMIN"})
    void shouldAllowAdmincancellation() throws Exception {
        Booking booking = bookingRepository.save(Booking.builder()
                .startDate(LocalDateTime.now().plusDays(4))
                .endDate(LocalDateTime.now().plusDays(4).plusHours(1))
                .resource(meetingRoom)
                .user(user)
                .status(Status.CONFIRMED)
                .build());

        mockMvc.perform(delete("/api/v1/bookings/" + booking.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Un utilisateur devrait pouvoir récupérer ses propres réservations")
    @WithMockUser(username = "employee@corp.com")
    void shouldGetMyBookings() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/mine"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Un ADMIN devrait pouvoir récupérer toutes les réservations")
    @WithMockUser(username = "admin@corp.com", roles = {"ADMIN"})
    void shouldGetAllBookingsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un simple USER ne devrait pas pouvoir accéder à la liste globale")
    @WithMockUser(username = "employee@corp.com", roles = {"EMPLOYEE"})
    void shouldDenyAllBookingsForSimpleUser() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isForbidden());
    }
}
