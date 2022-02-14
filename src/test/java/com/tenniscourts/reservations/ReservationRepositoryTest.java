package com.tenniscourts.reservations;

import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleRepository;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TennisCourtRepository tennisCourtRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Schedule schedule1, schedule2, schedule3;

    private static final LocalDateTime START_OF_NEXT_HOUR = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        TennisCourt tennisCourt1 = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Guest guest1 = Guest.builder()
                .name("Serena Williams")
                .build();
        guest1 = guestRepository.save(guest1);

        schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(START_OF_NEXT_HOUR)
                .endDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .build();
        schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .endDateTime(START_OF_NEXT_HOUR.plusHours(2L))
                .build();
        schedule3 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(2L))
                .endDateTime(START_OF_NEXT_HOUR.plusHours(3L))
                .build();
        schedule1 = scheduleRepository.save(schedule1);
        schedule2 = scheduleRepository.save(schedule2);
        schedule3 = scheduleRepository.save(schedule3);

        Reservation reservation1 = Reservation.builder()
                .guest(guest1)
                .schedule(schedule1)
                .value(BigDecimal.ZERO)
                .reservationStatus(ReservationStatus.READY_TO_PLAY)
                .build();
        Reservation reservation2 = Reservation.builder()
                .guest(guest1)
                .schedule(schedule2)
                .value(BigDecimal.ZERO)
                .reservationStatus(ReservationStatus.READY_TO_PLAY)
                .build();
        Reservation reservation3 = Reservation.builder()
                .guest(guest1)
                .schedule(schedule3)
                .value(BigDecimal.ZERO)
                .reservationStatus(ReservationStatus.CANCELLED)
                .build();
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
    }

    @AfterEach
    void destroy() {
        reservationRepository.deleteAll();
        scheduleRepository.deleteAll();
        guestRepository.deleteAll();
        tennisCourtRepository.deleteAll();
    }

    @Test
    void testFindBySchedule_Id() {
        List<Reservation> reservationList = reservationRepository.findBySchedule_Id(schedule1.getId());
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(1, reservationList.size());
        Assertions.assertNotNull(reservationList.get(0));
        Assertions.assertNotNull(reservationList.get(0).getSchedule());
        Assertions.assertEquals(schedule1, reservationList.get(0).getSchedule());
        Assertions.assertEquals(ReservationStatus.READY_TO_PLAY, reservationList.get(0).getReservationStatus());

        reservationList = reservationRepository.findBySchedule_Id(schedule3.getId());
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(1, reservationList.size());
        Assertions.assertNotNull(reservationList.get(0));
        Assertions.assertNotNull(reservationList.get(0).getSchedule());
        Assertions.assertEquals(schedule3, reservationList.get(0).getSchedule());
        Assertions.assertEquals(ReservationStatus.CANCELLED, reservationList.get(0).getReservationStatus());
    }

    @Test
    void testFindByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual() {
        List<Reservation> reservationList = reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                ReservationStatus.READY_TO_PLAY,
                START_OF_NEXT_HOUR,
                START_OF_NEXT_HOUR.plusHours(1L));
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(1, reservationList.size());
        Assertions.assertNotNull(reservationList.get(0));
        Assertions.assertNotNull(reservationList.get(0).getSchedule());
        Assertions.assertEquals(schedule1, reservationList.get(0).getSchedule());
        Assertions.assertEquals(ReservationStatus.READY_TO_PLAY, reservationList.get(0).getReservationStatus());

        reservationList = reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                ReservationStatus.READY_TO_PLAY,
                START_OF_NEXT_HOUR,
                START_OF_NEXT_HOUR.plusHours(10L));
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(2, reservationList.size());

        reservationList = reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                ReservationStatus.CANCELLED,
                START_OF_NEXT_HOUR,
                START_OF_NEXT_HOUR.plusHours(4L));
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(1, reservationList.size());

        reservationList = reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                ReservationStatus.RESCHEDULED,
                START_OF_NEXT_HOUR,
                START_OF_NEXT_HOUR.plusHours(4L));
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(0, reservationList.size());
    }


}
