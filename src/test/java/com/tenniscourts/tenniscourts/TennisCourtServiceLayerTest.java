package com.tenniscourts.tenniscourts;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TennisCourtServiceLayerTest {

    @MockBean
    private TennisCourtRepository tennisCourtRepository;

    @MockBean
    private ScheduleService scheduleService;

    private TennisCourtService tennisCourtService;

    private TennisCourtMapper tennisCourtMapper;

    @BeforeEach
    void setUp() {
        tennisCourtMapper = new TennisCourtMapperImpl();
        tennisCourtService = new TennisCourtService(tennisCourtRepository, scheduleService, tennisCourtMapper);
    }

    @Test
    void testAddTennisCourt() {
        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder()
                .id(1L)
                .name("Court 1")
                .build();
        TennisCourt tennisCourt = tennisCourtMapper.map(tennisCourtDTO);
        Mockito.when(tennisCourtRepository.saveAndFlush(tennisCourt)).thenReturn(tennisCourt);

        TennisCourtDTO addedTennisCourt = tennisCourtService.addTennisCourt(tennisCourtDTO);
        Assertions.assertNotNull(addedTennisCourt);
        Assertions.assertEquals(tennisCourtDTO.getId(), addedTennisCourt.getId());
    }

    @Test
    void testFindTennisCourtById() {
        Mockito.when(tennisCourtRepository.findById(1L)).thenReturn(Optional.of(new TennisCourt("Court 1")));
        Mockito.when(tennisCourtRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertNotNull(tennisCourtService.findTennisCourtById(1L));

        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> tennisCourtService.findTennisCourtById(2L));
        Assertions.assertEquals("Tennis Court not found.", exception.getMessage());
    }

    @Test
    void testFindTennisCourtWithSchedulesById() {
        List<ScheduleDTO> schedules = new ArrayList<>();
        schedules.add(new ScheduleDTO());
        Mockito.when(tennisCourtRepository.findById(1L)).thenReturn(Optional.of(new TennisCourt("Court 1")));
        Mockito.when(scheduleService.findSchedulesByTennisCourtId(Mockito.any(Long.class))).thenReturn(schedules);
        Mockito.when(tennisCourtRepository.findById(2L)).thenReturn(Optional.empty());

        TennisCourtDTO tennisCourtDTO = tennisCourtService.findTennisCourtWithSchedulesById(1L);
        Assertions.assertNotNull(tennisCourtDTO);
        Assertions.assertNotNull(tennisCourtDTO.getTennisCourtSchedules());
        Assertions.assertEquals(1, tennisCourtDTO.getTennisCourtSchedules().size());

        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> tennisCourtService.findTennisCourtWithSchedulesById(2L));
        Assertions.assertEquals("Tennis Court not found.", exception.getMessage());
    }
}
