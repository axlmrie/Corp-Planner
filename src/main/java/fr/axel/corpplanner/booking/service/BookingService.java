package fr.axel.corpplanner.booking.service;

import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.booking.domain.Status;
import fr.axel.corpplanner.booking.dto.BookingRequest;
import fr.axel.corpplanner.booking.dto.BookingResponse;
import fr.axel.corpplanner.booking.mapper.BookingMapper;
import fr.axel.corpplanner.booking.repository.BookingRepository;
import fr.axel.corpplanner.config.BookingConflictException;
import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    public BookingResponse createBooking(BookingRequest request, UserDetails connectedUser) {

        User user = userRepository.findByEmail(connectedUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new RuntimeException("Ressource introuvable"));

        if (request.startDate().isAfter(request.endDate())) {
            throw new RuntimeException("La date de début doit être avant la fin");
        }

        boolean hasConflict = bookingRepository.hasConflictingBooking(
                request.resourceId(),
                request.startDate(),
                request.endDate()
        );

        if (hasConflict) {
            throw new BookingConflictException("Cette ressource est déjà réservée sur ce créneau.");
        }

        Booking booking = Booking.builder()
                .startDate(request.startDate())
                .endDate(request.endDate())
                .resource(resource)
                .user(user)
                .status(Status.WAITING)
                .build();

        return bookingMapper.mapToResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getBookings(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        return bookingRepository.findAllByUser_Id(user.getId()).stream().map(bookingMapper :: mapToResponse).toList();
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(bookingMapper :: mapToResponse).toList();
    }

    public void cancelBooking(Long bookingId, UserDetails connectedUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        User currentUser = userRepository.findByEmail(connectedUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());

        boolean isAdmin = currentUser.getRoles().contains(Role.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Vous n'avez pas le droit d'annuler cette réservation.");
        }

        if (booking.getStatus() == Status.CANCELLED) {
            throw new RuntimeException("Cette réservation est déjà annulée.");
        }

        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);
    }

}
