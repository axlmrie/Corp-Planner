package fr.axel.corpplanner.booking.mapper;

import fr.axel.corpplanner.booking.domain.Booking;
import fr.axel.corpplanner.booking.dto.BookingResponse;
import org.springframework.stereotype.Component;


@Component
public class BookingMapper {

    public BookingResponse mapToResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingResponse(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus().name(),
                booking.getResource().getName()
        );
    }
}

