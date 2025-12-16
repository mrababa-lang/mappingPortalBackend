package com.slashdata.vehicleportal.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @BeforeEach
    void setUp() {
        vehicleTypeRepository.deleteAll();
    }

    @Test
    void listVehicleTypesIsAccessibleWithoutAuthentication() throws Exception {
        VehicleType type = new VehicleType();
        type.setName("Sedan");
        vehicleTypeRepository.save(type);

        mockMvc.perform(get("/api/types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].name").value("Sedan"));
    }
}
