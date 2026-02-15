package fr.axel.corpplanner.booking;

import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.booking.dto.BookingRequest;
import fr.axel.corpplanner.booking.dto.BookingResponse;
import fr.axel.corpplanner.booking.service.BookingService;
import fr.axel.corpplanner.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Créer une réservation")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body( bookingService.createBooking(request, userDetails));
    }
    @GetMapping(path = "/mine")
    @Operation(summary = "Récupérer uniquement mes réservations")
    public ResponseEntity<List<BookingResponse>> getAllBookings(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getBookings(userDetails));
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les réservations de l'entreprise")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        bookingService.cancelBooking(id, userDetails);
        return ResponseEntity.noContent().build();
    }

}
