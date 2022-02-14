package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
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
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class GuestServiceLayerTest {

    @MockBean
    private GuestRepository guestRepository;

    private GuestMapper guestMapper;

    private GuestService guestService;

    @BeforeEach
    void setUp() {
        guestMapper = new GuestMapperImpl();
        guestService = new GuestService(guestRepository, guestMapper);
    }

    @Test
    void testAddGuest() {
        GuestDTO guestDTO = GuestDTO.builder()
                .id(1L)
                .name("Simona Halep")
                .build();
        Guest guest = guestMapper.map(guestDTO);
        Mockito.when(guestRepository.saveAndFlush(guest)).thenReturn(guest);

        GuestDTO addedGuest = guestService.addGuest(guestDTO);
        Assertions.assertNotNull(addedGuest);
        Assertions.assertEquals(1L, addedGuest.getId());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> guestService.addGuest(new GuestDTO()));
        Assertions.assertEquals("Guest id is null", exception.getMessage());
    }

    @Test
    void testFindGuestById() {
        Mockito.when(guestRepository.findById(1L)).thenReturn(Optional.of(new Guest()));
        Mockito.when(guestRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertNotNull(guestService.findGuestById(1L));

        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> guestService.findGuestById(2L));
        Assertions.assertEquals("Guest not found.", exception.getMessage());
    }

    @Test
    void testFindGuestByName() {
        Mockito.when(guestRepository.findAllByName("Roger Federer")).thenReturn(new ArrayList<>());
        Assertions.assertNotNull(guestService.findGuestsByName("Roger Federer"));
    }

    @Test
    void testListAllGuests() {
        Mockito.when(guestRepository.findAll()).thenReturn(new ArrayList<>());
        Assertions.assertNotNull(guestService.listAllGuests());
    }

    @Test
    void testUpdateGuest() {
        GuestDTO guestDTO = GuestDTO.builder()
                .id(1L)
                .name("Simona Halep")
                .build();
        GuestDTO newGuestDTO = GuestDTO.builder()
                .id(1L)
                .name("Serena Williams")
                .build();
        Guest guest = guestMapper.map(guestDTO);
        Guest newGuest = guestMapper.map(newGuestDTO);
        Mockito.when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        Mockito.when(guestRepository.saveAndFlush(newGuest)).thenReturn(newGuest);

        GuestDTO updatedGuestDTO = guestService.updateGuest(newGuestDTO);
        Assertions.assertNotNull(updatedGuestDTO);
        Assertions.assertEquals("Serena Williams", updatedGuestDTO.getName());

        GuestDTO guest2DTO = GuestDTO.builder()
                .name("Roger Federer")
                .build();
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> guestService.updateGuest(guest2DTO));
        Assertions.assertEquals("Guest id is null", exception.getMessage());
    }
}
