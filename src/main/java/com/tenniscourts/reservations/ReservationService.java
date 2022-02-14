package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.GuestDTO;
import com.tenniscourts.guests.GuestMapper;
import com.tenniscourts.guests.GuestService;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleMapper;
import com.tenniscourts.schedules.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final GuestService guestService;
    private final ScheduleService scheduleService;

    private final ReservationMapper reservationMapper;
    private final GuestMapper guestMapper;
    private final ScheduleMapper scheduleMapper;

    private final BigDecimal RESERVATION_DEPOSIT = new BigDecimal(10);

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        GuestDTO guestDTO = guestService.findGuestById(createReservationRequestDTO.getGuestId());
        ScheduleDTO scheduleDTO = scheduleService.findSchedule(createReservationRequestDTO.getScheduleId());
        List<Reservation> reservationList = reservationRepository.findBySchedule_Id(createReservationRequestDTO.getScheduleId());

        validateBooking(reservationList, scheduleDTO);

        Reservation reservation = Reservation.builder()
                .guest(guestMapper.map(guestDTO))
                .schedule(scheduleMapper.map(scheduleDTO))
                .value(RESERVATION_DEPOSIT)
                .reservationStatus(ReservationStatus.READY_TO_PLAY)
                .build();

        return reservationMapper.map(reservationRepository.save(reservation));
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellation(reservation);

            BigDecimal refundValue = getRefundValue(reservation);
            return this.updateReservation(reservation, refundValue, ReservationStatus.CANCELLED);

        }).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation reschedule(Reservation reservation) {
        this.validateCancellation(reservation);

        BigDecimal refundValue = getRefundValue(reservation);
        return this.updateReservation(reservation, refundValue, ReservationStatus.RESCHEDULED);
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    private void validateBooking(List<Reservation> reservations, ScheduleDTO scheduleDTO) {
        if (hasReservation(reservations)) {
            throw new AlreadyExistsEntityException("Reservation already exists for"
                    + " tennis court " + scheduleDTO.getTennisCourt().getName()
                    + ", startDateTime = " + scheduleDTO.getStartDateTime()
                    + ", endDateTime = " + scheduleDTO.getEndDateTime());
        }

        if (scheduleDTO.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date and time is in the past");
        }
    }

    public BigDecimal getRefundValue(Reservation reservation) {
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());

        if (minutes >= 1440) {
            return reservation.getValue();
        } else if (minutes >= 720) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.75));
        } else if (minutes >= 120) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.5));
        } else if (minutes >= 1) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.25));
        }

        return BigDecimal.ZERO;
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, RescheduleReservationRequestDTO rescheduleReservationRequestDTO) {
        Reservation previousReservation = reservationMapper.map(findReservation(previousReservationId));
        Long scheduleId = rescheduleReservationRequestDTO.getScheduleId();

        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule id cannot be null.");
        }

        if (scheduleId.equals(previousReservation.getSchedule().getId())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        previousReservation = reschedule(previousReservation);
        reservationRepository.saveAndFlush(previousReservation);

        ReservationDTO newReservation = bookReservation(CreateReservationRequestDTO.builder()
                .guestId(previousReservation.getGuest().getId())
                .scheduleId(scheduleId)
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));
        return newReservation;
    }

    private boolean hasReservation(List<Reservation> reservationList) {
        return reservationList
                .stream()
                .anyMatch(reservation -> reservation.getReservationStatus().equals(ReservationStatus.READY_TO_PLAY));
    }

    public List<ReservationDTO> showPastReservations() {
        return reservationMapper.map(reservationRepository.findAllBySchedule_StartDateTimeLessThanEqual(LocalDateTime.now()));
    }
}
