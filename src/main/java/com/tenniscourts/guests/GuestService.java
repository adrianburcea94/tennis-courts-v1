package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public GuestDTO addGuest(GuestDTO guestDTO) {
        if (guestDTO.getId() == null) {
            throw new IllegalArgumentException("Guest id is null");
        }

        return guestMapper.map(guestRepository.saveAndFlush(guestMapper.map(guestDTO)));
    }

    public GuestDTO findGuestById(Long id) {
        return guestRepository.findById(id).map(guestMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });
    }

    public List<GuestDTO> findGuestsByName(String guestName) {
        return guestMapper.map(guestRepository.findAllByName(guestName));
    }

    public List<GuestDTO> listAllGuests() {
        return guestMapper.map(guestRepository.findAll());
    }

    public GuestDTO updateGuest(GuestDTO newGuestDTO) {
        if (newGuestDTO.getId() == null) {
            throw new IllegalArgumentException("Guest id is null");
        }

        return guestRepository.findById(newGuestDTO.getId()).map(guestMapper::map).map(guestDTO -> {
            guestDTO.setName(newGuestDTO.getName());

            return addGuest(guestDTO);
        }).orElseGet(() -> addGuest(newGuestDTO));
    }

    public void deleteGuest(Long guestId) {
        guestRepository.deleteById(guestId);
    }
}
