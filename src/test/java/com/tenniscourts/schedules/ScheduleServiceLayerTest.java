package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ScheduleServiceLayerTest {

    private static final LocalDateTime START_OF_NEXT_HOUR = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);
    @MockBean
    private ScheduleRepository scheduleRepository;
    @MockBean
    private TennisCourtRepository tennisCourtRepository;
    private ScheduleMapper scheduleMapper;
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleMapper = new ScheduleMapperImpl();
        scheduleService = new ScheduleService(scheduleRepository, tennisCourtRepository, scheduleMapper);
    }

    @Test
    void testAddSchedule() {
        TennisCourt tennisCourt1 = new TennisCourt("Court 1");
        tennisCourt1.setId(1L);
        TennisCourt tennisCourt2 = new TennisCourt("Court 2");
        tennisCourt1.setId(2L);

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(START_OF_NEXT_HOUR)
                .endDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .build();

        Mockito.when(tennisCourtRepository.findById(1L)).thenReturn(Optional.of(tennisCourt1));
        Mockito.when(tennisCourtRepository.findById(2L)).thenReturn(Optional.of(tennisCourt2));
        Mockito.when(tennisCourtRepository.findById(4L)).thenReturn(Optional.empty());

        Mockito.when(scheduleRepository.saveAndFlush(Mockito.any(Schedule.class))).thenReturn(schedule1);
        Mockito.when(scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(1L, START_OF_NEXT_HOUR.plusHours(1L))).thenReturn(null);
        Mockito.when(scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(2L, START_OF_NEXT_HOUR.plusHours(1L))).thenReturn(new Schedule());

        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> scheduleService.addSchedule(1L, new CreateScheduleRequestDTO()));
        Assertions.assertEquals("Missing schedule start date and time", illegalArgumentException.getMessage());

        CreateScheduleRequestDTO createScheduleRequestDTO1 = new CreateScheduleRequestDTO();
        createScheduleRequestDTO1.setStartDateTime(START_OF_NEXT_HOUR.minusHours(1L));
        illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> scheduleService.addSchedule(1L, createScheduleRequestDTO1));
        Assertions.assertEquals("Start date and time is in the past", illegalArgumentException.getMessage());

        CreateScheduleRequestDTO createScheduleRequestDTO2 = new CreateScheduleRequestDTO();
        createScheduleRequestDTO2.setStartDateTime(START_OF_NEXT_HOUR.plusHours(1L));
        EntityNotFoundException entityNotFoundException = Assertions.assertThrows(EntityNotFoundException.class, () -> scheduleService.addSchedule(4L, createScheduleRequestDTO2));
        Assertions.assertEquals("Tennis Court not found.", entityNotFoundException.getMessage());

        AlreadyExistsEntityException alreadyExistsEntityException = Assertions.assertThrows(AlreadyExistsEntityException.class, () -> scheduleService.addSchedule(2L, createScheduleRequestDTO2));
        Assertions.assertEquals("The schedule slot is already taken: " + createScheduleRequestDTO2.getStartDateTime(), alreadyExistsEntityException.getMessage());

        ScheduleDTO addedSchedule = scheduleService.addSchedule(1L, createScheduleRequestDTO2);
        Assertions.assertNotNull(addedSchedule);
    }

    @Test
    void testFindSchedulesByDates() {
        List<Schedule> scheduleList1 = new ArrayList<>();
        List<Schedule> scheduleList2 = new ArrayList<>();
        scheduleList2.add(new Schedule());
        Mockito.when(scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(START_OF_NEXT_HOUR, START_OF_NEXT_HOUR.plusHours(1L))).thenReturn(scheduleList1);
        Mockito.when(scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(START_OF_NEXT_HOUR.plusHours(2L), START_OF_NEXT_HOUR.plusHours(4L))).thenReturn(scheduleList2);

        List<ScheduleDTO> scheduleDTOList = scheduleService.findSchedulesByDates(START_OF_NEXT_HOUR, START_OF_NEXT_HOUR.plusHours(1L));
        Assertions.assertNotNull(scheduleDTOList);
        Assertions.assertEquals(0, scheduleDTOList.size());

        scheduleDTOList = scheduleService.findSchedulesByDates(START_OF_NEXT_HOUR.plusHours(2L), START_OF_NEXT_HOUR.plusHours(4L));
        Assertions.assertNotNull(scheduleDTOList);
        Assertions.assertEquals(1, scheduleDTOList.size());
    }

    @Test
    void testFindSchedule() {
        Mockito.when(scheduleRepository.findById(1L)).thenReturn(Optional.of(new Schedule()));
        Mockito.when(scheduleRepository.findById(2L)).thenReturn(Optional.empty());

        ScheduleDTO scheduleDTO = scheduleService.findSchedule(1L);
        Assertions.assertNotNull(scheduleDTO);

        EntityNotFoundException entityNotFoundException = Assertions.assertThrows(EntityNotFoundException.class, () -> scheduleService.findSchedule(2L));
        Assertions.assertEquals("Schedule not found.", entityNotFoundException.getMessage());
    }

    @Test
    void testFindSchedulesByTennisCourtId() {
        List<Schedule> scheduleList = new ArrayList<>();
        scheduleList.add(new Schedule());
        Mockito.when(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(1L)).thenReturn(scheduleList);

        List<ScheduleDTO> scheduleDTOList = scheduleService.findSchedulesByTennisCourtId(1L);
        Assertions.assertNotNull(scheduleDTOList);
        Assertions.assertEquals(1, scheduleDTOList.size());

        scheduleDTOList = scheduleService.findSchedulesByTennisCourtId(2L);
        Assertions.assertNotNull(scheduleDTOList);
        Assertions.assertEquals(0, scheduleDTOList.size());
    }
}
