package com.tenniscourts.guests;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/guests")
public class GuestController extends BaseRestController {

    private final GuestService guestService;

    @ApiOperation(value = "Find a guest by id")
    @GetMapping(value = "/{id}")
    public ResponseEntity<GuestDTO> findGuestById(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.findGuestById(id));
    }

    @ApiOperation(value = "List all the guests")
    @GetMapping
    public ResponseEntity<List<GuestDTO>> listAllGuests(@RequestParam(value = "name", required = false) String name) {
        if (name != null) {
            return ResponseEntity.ok(guestService.findGuestsByName(name));
        }

        return ResponseEntity.ok(guestService.listAllGuests());
    }

    @ApiOperation(value = "Add a guest")
    @PostMapping
    public ResponseEntity<Void> addGuest(@RequestBody GuestDTO guestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.addGuest(guestDTO).getId())).build();
    }

    @ApiOperation(value = "Update a guest")
    @PutMapping
    public ResponseEntity<GuestDTO> updateGuest(@RequestBody GuestDTO guestDTO) {
        return ResponseEntity.ok(guestService.updateGuest(guestDTO));
    }

    @ApiOperation(value = "Delete a guest by id")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteGuestById(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }
}
