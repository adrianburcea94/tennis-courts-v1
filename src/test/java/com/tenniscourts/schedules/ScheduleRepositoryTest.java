package com.tenniscourts.schedules;

import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    TennisCourtRepository tennisCourtRepository;

    private Long tennisCourt1Id, tennisCourt2Id;

    private static final LocalDateTime START_OF_NEXT_HOUR = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        TennisCourt tennisCourt1 = tennisCourtRepository.save(new TennisCourt("Court 1"));
        tennisCourt1Id = tennisCourt1.getId();

        TennisCourt tennisCourt2 = tennisCourtRepository.save(new TennisCourt("Court 2"));
        tennisCourt2Id = tennisCourt2.getId();

        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(START_OF_NEXT_HOUR)
                .endDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .build();

        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .endDateTime(START_OF_NEXT_HOUR.plusHours(2L))
                .build();

        Schedule schedule3 = Schedule.builder()
                .tennisCourt(tennisCourt2)
                .startDateTime(START_OF_NEXT_HOUR)
                .endDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .build();

        Schedule schedule4 = Schedule.builder()
                .tennisCourt(tennisCourt2)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(1L))
                .endDateTime(START_OF_NEXT_HOUR.plusHours(2L))
                .build();

        Schedule schedule5 = Schedule.builder()
                .tennisCourt(tennisCourt2)
                .startDateTime(START_OF_NEXT_HOUR.plusHours(4L))
                .endDateTime(START_OF_NEXT_HOUR.plusHours(5L))
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);
        scheduleRepository.save(schedule4);
        scheduleRepository.save(schedule5);
    }

    @AfterEach
    void destroy() {
        scheduleRepository.deleteAll();
        tennisCourtRepository.deleteAll();
    }

    @Test
    void testFindByTennisCourt_IdOrderByStartDateTime() {
        // SCHEDULE for TENNIS COURT 1
        List<Schedule> scheduleList = scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourt1Id);
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(2, scheduleList.size());
        Assertions.assertNotNull(scheduleList.get(0));
        Assertions.assertEquals(START_OF_NEXT_HOUR, scheduleList.get(0).getStartDateTime());
        Assertions.assertNotNull(scheduleList.get(1));
        Assertions.assertEquals(START_OF_NEXT_HOUR.plusHours(1), scheduleList.get(1).getStartDateTime());

        // SCHEDULE for TENNIS COURT 2
        scheduleList = scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourt2Id);
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(3, scheduleList.size());
        Assertions.assertNotNull(scheduleList.get(0));
        Assertions.assertEquals(START_OF_NEXT_HOUR, scheduleList.get(0).getStartDateTime());
        Assertions.assertNotNull(scheduleList.get(1));
        Assertions.assertEquals(START_OF_NEXT_HOUR.plusHours(1), scheduleList.get(1).getStartDateTime());
        Assertions.assertNotNull(scheduleList.get(2));
        Assertions.assertEquals(START_OF_NEXT_HOUR.plusHours(4), scheduleList.get(2).getStartDateTime());

        long tennisCourt3Id = tennisCourt1Id - tennisCourt2Id;
        scheduleList = scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourt3Id);
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(0, scheduleList.size());
    }

    @Test
    void testFindByTennisCourt_IdAndStartDateTimeEquals() {
        Schedule schedule = scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(tennisCourt1Id, START_OF_NEXT_HOUR);
        Assertions.assertNotNull(schedule);
        Assertions.assertEquals(START_OF_NEXT_HOUR.plusHours(1L), schedule.getEndDateTime());

        schedule = scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(tennisCourt2Id, START_OF_NEXT_HOUR.plusHours(4));
        Assertions.assertNotNull(schedule);
        Assertions.assertEquals(START_OF_NEXT_HOUR.plusHours(5L), schedule.getEndDateTime());

        schedule = scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(tennisCourt1Id, START_OF_NEXT_HOUR.plusHours(10L));
        Assertions.assertNull(schedule);
    }

    @Test
    void testFindAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual() {
        List<Schedule> scheduleList = scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(START_OF_NEXT_HOUR, START_OF_NEXT_HOUR.plusHours(5L));
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(5, scheduleList.size());

        scheduleList = scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(START_OF_NEXT_HOUR, START_OF_NEXT_HOUR.plusHours(2L));
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(4, scheduleList.size());

        scheduleList = scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(START_OF_NEXT_HOUR.plusHours(1L), START_OF_NEXT_HOUR.plusHours(2L));
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(2, scheduleList.size());

        scheduleList = scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(START_OF_NEXT_HOUR.plusHours(9L), START_OF_NEXT_HOUR.plusHours(10L));
        Assertions.assertNotNull(scheduleList);
        Assertions.assertEquals(0, scheduleList.size());
    }
}