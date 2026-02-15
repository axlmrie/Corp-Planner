package fr.axel.corpplanner.booking.service;

import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.booking.dto.BookingRequest;
import fr.axel.corpplanner.booking.repository.BookingRepository;
import fr.axel.corpplanner.resource.domain.Resource;
import fr.axel.corpplanner.resource.repository.ResourceRepository;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public Booking createBooking(BookingRequest request, UserDetails connectedUser) {

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
            throw new RuntimeException("Cette ressource est déjà réservée sur ce créneau.");
        }

        Booking booking = Booking.builder()
                .startDate(request.startDate())
                .endDate(request.endDate())
                .resource(resource)
                .user(user)
                .build();

        return bookingRepository.save(booking);
    }
}
