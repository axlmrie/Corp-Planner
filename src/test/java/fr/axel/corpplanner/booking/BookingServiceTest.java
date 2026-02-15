package fr.axel.corpplanner.booking;

import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.booking.domain.Status;
import fr.axel.corpplanner.booking.dto.BookingRequest;
import fr.axel.corpplanner.booking.dto.BookingResponse;
import fr.axel.corpplanner.booking.mapper.BookingMapper;
import fr.axel.corpplanner.booking.repository.BookingRepository;
import fr.axel.corpplanner.booking.service.BookingService;
import fr.axel.corpplanner.config.BookingConflictException;
import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    @Mock private UserDetails userDetails;


    @Test
    @DisplayName("Devrait créer une réservation quand tout est OK")
    void shouldCreateBookingSuccess() {
        BookingRequest request = new BookingRequest(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                1L
        );

        User user = User.builder().id(1L).email("test@test.com").build();
        Resource resource = Resource.builder().id(1L).name("Salle Test").build();

        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(bookingRepository.hasConflictingBooking(any(), any(), any())).thenReturn(false);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(123L);
            return b;
        });


        BookingResponse fakeResponse = new BookingResponse(
                123L, request.startDate(), request.endDate(), "WAITING", "Salle Test"
        );
        when(bookingMapper.mapToResponse(any(Booking.class))).thenReturn(fakeResponse);

        BookingResponse result = bookingService.createBooking(request, userDetails);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("WAITING");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Devrait lancer une erreur si les dates sont incohérentes")
    void shouldThrowIfDatesInvalid() {
        BookingRequest request = new BookingRequest(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                1L
        );

        when(userDetails.getUsername()).thenReturn("a@a.com");
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(new User()));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(new Resource()));

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(request, userDetails));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait lancer BookingConflictException si créneau pris")
    void shouldThrowIfConflict() {
        BookingRequest request = new BookingRequest(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L);

        when(userDetails.getUsername()).thenReturn("a@a.com");
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(new User()));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(new Resource()));

        when(bookingRepository.hasConflictingBooking(any(), any(), any())).thenReturn(true);

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(request, userDetails));
        verify(bookingRepository, never()).save(any());
    }


    @Test
    @DisplayName("Le propriétaire doit pouvoir annuler sa réservation")
    void ownerShouldCancelBooking() {
        User owner = User.builder().id(10L).email("owner@test.com").roles(Set.of(Role.EMPLOYEE)).build();
        Booking booking = Booking.builder().id(55L).user(owner).status(Status.CONFIRMED).build();

        when(bookingRepository.findById(55L)).thenReturn(Optional.of(booking));
        when(userDetails.getUsername()).thenReturn("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        bookingService.cancelBooking(55L, userDetails);

        assertThat(booking.getStatus()).isEqualTo(Status.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("Un tiers ne doit PAS pouvoir annuler la réservation d'un autre")
    void strangerShouldNotCancelBooking() {
        User owner = User.builder().id(10L).build();
        User hacker = User.builder().id(99L).email("hacker@test.com").roles(Set.of(Role.EMPLOYEE)).build();

        Booking booking = Booking.builder().id(55L).user(owner).status(Status.CONFIRMED).build();

        when(bookingRepository.findById(55L)).thenReturn(Optional.of(booking));
        when(userDetails.getUsername()).thenReturn("hacker@test.com");
        when(userRepository.findByEmail("hacker@test.com")).thenReturn(Optional.of(hacker));

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(55L, userDetails));

        assertThat(booking.getStatus()).isEqualTo(Status.CONFIRMED);
        verify(bookingRepository, never()).save(any());
    }
    @Test
    @DisplayName("Devrait lancer une erreur si l'utilisateur n'est pas trouvé")
    void shouldThrowWhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("unknown@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(new BookingRequest(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L), userDetails)
        );
    }

    @Test
    @DisplayName("Devrait lancer une erreur si la ressource n'existe pas")
    void shouldThrowWhenResourceNotFound() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        when(resourceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(new BookingRequest(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L), userDetails)
        );
    }

    @Test
    @DisplayName("Devrait lancer une erreur si on annule une réservation déjà annulée")
    void shouldThrowIfAlreadyCancelled() {
        User owner = User.builder().id(1L).email("owner@test.com").roles(Set.of(Role.EMPLOYEE)).build();
        Booking booking = Booking.builder().id(1L).user(owner).status(Status.CANCELLED).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userDetails.getUsername()).thenReturn("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                bookingService.cancelBooking(1L, userDetails)
        );
        assertThat(exception.getMessage()).isEqualTo("Cette réservation est déjà annulée.");
    }

    @Test
    @DisplayName("Devrait récupérer les réservations de l'utilisateur connecté")
    void shouldGetMyBookings() {
        User user = User.builder().id(1L).email("test@test.com").build();
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByUser_Id(1L)).thenReturn(List.of(new Booking()));
        when(bookingMapper.mapToResponse(any())).thenReturn(new BookingResponse(1L, null, null, "WAITING", "Salle"));

        List<BookingResponse> results = bookingService.getBookings(userDetails);

        assertThat(results).hasSize(1);
        verify(bookingRepository).findAllByUser_Id(1L);
    }
}