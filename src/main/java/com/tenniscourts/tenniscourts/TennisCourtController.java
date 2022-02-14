package com.tenniscourts.tenniscourts;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/tennisCourts")
public class TennisCourtController extends BaseRestController {

    private final TennisCourtService tennisCourtService;

    @ApiOperation(value = "Add a tennis court")
    @PostMapping
    public ResponseEntity<Void> addTennisCourt(@RequestBody TennisCourtDTO tennisCourtDTO) {
        return ResponseEntity.created(locationByEntity(tennisCourtService.addTennisCourt(tennisCourtDTO).getId())).build();
    }

    @ApiOperation(value = "Find a tennis court by id")
    @GetMapping(value = "/{id}")
    public ResponseEntity<TennisCourtDTO> findTennisCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(tennisCourtService.findTennisCourtById(id));
    }

    @ApiOperation(value = "Find tennis court with schedules by id")
    @GetMapping(value = "/{id}/withSchedules")
    public ResponseEntity<TennisCourtDTO> findTennisCourtWithSchedulesById(@PathVariable Long id) {
        return ResponseEntity.ok(tennisCourtService.findTennisCourtWithSchedulesById(id));
    }
}
