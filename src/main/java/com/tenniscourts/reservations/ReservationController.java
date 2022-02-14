package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/reservations")
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @ApiOperation(value = "Book a reservation")
    @PostMapping
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    @ApiOperation(value = "Find a reservation by id")
    @GetMapping(value = "/{id}")
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findReservation(id));
    }

    @ApiOperation(value = "Cancel a reservation by id")
    @PutMapping(value = "/{id}/cancel")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @ApiOperation(value = "Reschedule a reservation by id")
    @PutMapping(value = "/{id}")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long id, @RequestBody RescheduleReservationRequestDTO rescheduleReservationRequestDTO) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(id, rescheduleReservationRequestDTO));
    }

    @ApiOperation(value = "List all past reservations")
    @GetMapping(value = "/history")
    public ResponseEntity<List<ReservationDTO>> showPastReservations() {
        return ResponseEntity.ok(reservationService.showPastReservations());
    }

}
