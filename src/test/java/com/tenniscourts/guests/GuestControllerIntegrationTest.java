package com.tenniscourts.guests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureMockMvc
@SpringBootTest
public class GuestControllerIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test that a Tennis Court Admin is able to:
     * - create / update / delete guests
     * - find guests by id / name
     * - list all the guests
     */
    @Test
    void testGuestManagement() throws Exception {
        GuestDTO guestDTO = GuestDTO.builder()
                .id(3L)
                .name("Novak Djokovic")
                .build();

        mockMvc.perform(post(BASE_URL + "/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(guestDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/guests?name=Novak Djokovic")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Novak Djokovic")));

        guestDTO.setName("N Djokovic");

        mockMvc.perform(put(BASE_URL + "/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(guestDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/guests/3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("N Djokovic")))
                .andExpect((jsonPath("$.id", is(3))));

        mockMvc.perform(get(BASE_URL + "/guests")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(delete(BASE_URL + "/guests/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(guestDTO)))
                .andExpect(status().isNoContent());
    }
}
