package com.tenniscourts.reservations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleRepository;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureMockMvc
@SpringBootTest
public class ReservationControllerIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final LocalDateTime START_OF_NEXT_HOUR = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);
    private final BigDecimal RESERVATION_FEE = new BigDecimal("10.00");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TennisCourtRepository tennisCourtRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void destroy() {
        reservationRepository.deleteAll();
        scheduleRepository.deleteAll();
        guestRepository.deleteAll();
        tennisCourtRepository.deleteAll();
    }

    /**
     * Test that a user is able to:
     * - book a reservation for one or more tennis courts at a given date schedule
     * - reschedule a reservation
     * - cancel a reservation
     *
     * @throws Exception
     */
    @Test
    void testManageReservation() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Guest guest = Guest.builder()
                .name("Serena Williams")
                .build();
        guest = guestRepository.save(guest);

        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR)
                .endDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .build();
        schedule = scheduleRepository.save(schedule);

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO.builder()
                .guestId(guest.getId())
                .scheduleId(schedule.getId())
                .build();


        // Book a reservation
        mockMvc.perform(post(BASE_URL + "/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createReservationRequestDTO)))
                .andExpect(status().isCreated());

        List<Reservation> reservationList = reservationRepository.findBySchedule_Id(schedule.getId());
        Assertions.assertNotNull(reservationList);
        Assertions.assertEquals(1, reservationList.size());
        Assertions.assertNotNull(reservationList.get(0));
        Assertions.assertNotNull(reservationList.get(0).getId());
        Long reservationId = reservationList.get(0).getId();
        Reservation reservation = reservationRepository.findById(reservationId).get();
        Assertions.assertNotNull(reservation);

        // Cancel a reservation
        mockMvc.perform(put(BASE_URL + "/reservations/" + reservationId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        reservation = reservationRepository.findById(reservationId).get();
        Assertions.assertNotNull(reservation);
        Assertions.assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());


    }

    @Test
    void testRescheduleReservationFullRefund() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(25))
                .endDateTime(LocalDateTime.now().plusHours(26))
                .build();
        schedule1 = scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(48))
                .endDateTime(LocalDateTime.now().plusHours(49))
                .build();
        schedule2 = scheduleRepository.save(schedule2);

        Reservation reservation = createReservation(schedule1);
        RescheduleReservationRequestDTO rescheduleReservationRequestDTO = RescheduleReservationRequestDTO.builder()
                .scheduleId(schedule2.getId())
                .build();

        // Reschedule a reservation
        mockMvc.perform(put(BASE_URL + "/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rescheduleReservationRequestDTO)))
                .andExpect(status().isOk());

        // FULL REFUND
        reservation = reservationRepository.findById(reservation.getId()).get();
        Assertions.assertEquals(ReservationStatus.RESCHEDULED, reservation.getReservationStatus());
        Assertions.assertEquals(RESERVATION_FEE, reservation.getRefundValue());
    }

    @Test
    void testRescheduleReservationRefund25Percent() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR)
                .endDateTime(START_OF_NEXT_HOUR.plusHours(1).minusMinutes(1))
                .build();
        schedule1 = scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(48))
                .endDateTime(LocalDateTime.now().plusHours(49))
                .build();
        schedule2 = scheduleRepository.save(schedule2);

        Reservation reservation = createReservation(schedule1);
        RescheduleReservationRequestDTO rescheduleReservationRequestDTO = RescheduleReservationRequestDTO.builder()
                .scheduleId(schedule2.getId())
                .build();

        // Reschedule a reservation
        mockMvc.perform(put(BASE_URL + "/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rescheduleReservationRequestDTO)))
                .andExpect(status().isOk());

        // 25% REFUND
        reservation = reservationRepository.findById(reservation.getId()).get();
        Assertions.assertEquals(ReservationStatus.RESCHEDULED, reservation.getReservationStatus());
        Assertions.assertEquals(RESERVATION_FEE.doubleValue() * 0.25, reservation.getRefundValue().doubleValue());
    }

    @Test
    void testRescheduleReservationRefund50Percent() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(7))
                .endDateTime(LocalDateTime.now().plusHours(8))
                .build();
        schedule1 = scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(48))
                .endDateTime(LocalDateTime.now().plusHours(49))
                .build();
        schedule2 = scheduleRepository.save(schedule2);

        Reservation reservation = createReservation(schedule1);
        RescheduleReservationRequestDTO rescheduleReservationRequestDTO = RescheduleReservationRequestDTO.builder()
                .scheduleId(schedule2.getId())
                .build();

        // Reschedule a reservation
        mockMvc.perform(put(BASE_URL + "/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rescheduleReservationRequestDTO)))
                .andExpect(status().isOk());

        // 50% REFUND
        reservation = reservationRepository.findById(reservation.getId()).get();
        Assertions.assertEquals(ReservationStatus.RESCHEDULED, reservation.getReservationStatus());
        Assertions.assertEquals(RESERVATION_FEE.doubleValue() * 0.5, reservation.getRefundValue().doubleValue());
    }

    @Test
    void testRescheduleReservation75Refund() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(14))
                .endDateTime(LocalDateTime.now().plusHours(15))
                .build();
        schedule1 = scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(48))
                .endDateTime(LocalDateTime.now().plusHours(49))
                .build();
        schedule2 = scheduleRepository.save(schedule2);

        Reservation reservation = createReservation(schedule1);
        RescheduleReservationRequestDTO rescheduleReservationRequestDTO = RescheduleReservationRequestDTO.builder()
                .scheduleId(schedule2.getId())
                .build();

        // Reschedule a reservation
        mockMvc.perform(put(BASE_URL + "/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rescheduleReservationRequestDTO)))
                .andExpect(status().isOk());

        // 75% REFUND
        reservation = reservationRepository.findById(reservation.getId()).get();
        Assertions.assertEquals(ReservationStatus.RESCHEDULED, reservation.getReservationStatus());
        Assertions.assertEquals(RESERVATION_FEE.doubleValue() * 0.75, reservation.getRefundValue().doubleValue());
    }

    private Reservation createReservation(Schedule schedule) {

        Guest guest = Guest.builder()
                .name("Serena Williams")
                .build();
        guest = guestRepository.save(guest);

        Reservation reservation = Reservation.builder()
                .guest(guest)
                .schedule(schedule)
                .value(RESERVATION_FEE)
                .reservationStatus(ReservationStatus.READY_TO_PLAY)
                .build();

        return reservationRepository.save(reservation);
    }

    @Test
    void testListReservationHistory() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.minusHours(2))
                .endDateTime(START_OF_NEXT_HOUR.minusHours(1))
                .build();
        schedule1 = scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.minusHours(10))
                .endDateTime(START_OF_NEXT_HOUR.minusHours(9))
                .build();
        schedule2 = scheduleRepository.save(schedule2);

        Schedule schedule3 = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(10))
                .endDateTime(START_OF_NEXT_HOUR.plusHours(11))
                .build();
        schedule3 = scheduleRepository.save(schedule3);

        createReservation(schedule1);
        createReservation(schedule2);
        createReservation(schedule3);

        mockMvc.perform(get(BASE_URL + "/reservations/history")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }
}