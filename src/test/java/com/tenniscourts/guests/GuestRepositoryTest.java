package com.tenniscourts.guests;

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

import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class GuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @BeforeEach
    void setUp() {
        Guest guest1 = Guest.builder()
                .name("Novak Djokovic")
                .build();

        Guest guest2 = Guest.builder()
                .name("Serena Williams")
                .build();

        Guest guest3 = Guest.builder()
                .name("Novak Djokovic")
                .build();

        guestRepository.save(guest1);
        guestRepository.save(guest2);
        guestRepository.save(guest3);
    }

    @AfterEach
    void destroy() {
        guestRepository.deleteAll();
    }

    @Test
    void testFindAllByName() {
        List<Guest> guestList = guestRepository.findAllByName("Serena Williams");
        Assertions.assertNotNull(guestList);
        Assertions.assertEquals(1, guestList.size());
        Assertions.assertNotNull(guestList.get(0));
        Assertions.assertEquals("Serena Williams", guestList.get(0).getName());

        guestList = guestRepository.findAllByName("Novak Djokovic");
        Assertions.assertNotNull(guestList);
        Assertions.assertEquals(2, guestList.size());
        Assertions.assertNotNull(guestList.get(0));
        Assertions.assertEquals("Novak Djokovic", guestList.get(0).getName());
        Assertions.assertNotNull(guestList.get(1));
        Assertions.assertEquals("Novak Djokovic", guestList.get(1).getName());

        guestList = guestRepository.findAllByName("Venus Williams");
        Assertions.assertNotNull(guestList);
        Assertions.assertEquals(0, guestList.size());
    }


}
