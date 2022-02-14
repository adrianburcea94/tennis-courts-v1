package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtDTO;
import com.tenniscourts.tenniscourts.TennisCourtMapper;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TennisCourtRepository tennisCourtRepository;

    private final ScheduleMapper scheduleMapper;

    private final static Long PLAY_TIME_HOURS = 1L;

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        if (createScheduleRequestDTO.getStartDateTime() == null) {
            throw new IllegalArgumentException("Missing schedule start date and time");
        } else if (createScheduleRequestDTO.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date and time is in the past");
        } else {
            return scheduleMapper.map(scheduleRepository.saveAndFlush(createScheduleForTennisCourt(tennisCourtId, createScheduleRequestDTO)));
        }
    }

    private Schedule createScheduleForTennisCourt(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        TennisCourt tennisCourt = tennisCourtRepository.findById(tennisCourtId).orElseThrow(() -> {
            throw new EntityNotFoundException("Tennis Court not found.");
        });

        if (scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(tennisCourtId, createScheduleRequestDTO.getStartDateTime()) != null) {
            throw new AlreadyExistsEntityException("The schedule slot is already taken: " + createScheduleRequestDTO.getStartDateTime());
        } else {
            return Schedule.builder()
                    .tennisCourt(tennisCourt)
                    .startDateTime(createScheduleRequestDTO.getStartDateTime())
                    .endDateTime(createScheduleRequestDTO.getStartDateTime().plusHours(PLAY_TIME_HOURS))
                    .build();
        }
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(startDate, endDate));
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).map(scheduleMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        });
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }
}
