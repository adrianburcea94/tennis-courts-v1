package com.tenniscourts.schedules;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureMockMvc
@SpringBootTest
public class ScheduleControllerIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final LocalDateTime START_OF_NEXT_HOUR = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TennisCourtRepository tennisCourtRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @AfterEach
    void destroy() {
        scheduleRepository.deleteAll();
        tennisCourtRepository.deleteAll();
    }

    /**
     * Test that a Tennis Court admin is able to create schedules for a given tennis court
     */
    @Test
    void testCreateSchedulesForTennisCourt() throws Exception {
        TennisCourt tennisCourt = tennisCourtRepository.save(new TennisCourt("Court 1"));

        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO(tennisCourt.getId(), START_OF_NEXT_HOUR.plusHours(1));

        mockMvc.perform(post(BASE_URL + "/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScheduleRequestDTO)))
                .andExpect(status().isCreated());

        createScheduleRequestDTO = new CreateScheduleRequestDTO(tennisCourt.getId(), START_OF_NEXT_HOUR.plusHours(4));
        mockMvc.perform(post(BASE_URL + "/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScheduleRequestDTO)))
                .andExpect(status().isCreated());

        Assertions.assertEquals(2, scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourt.getId()).size());
    }
}
