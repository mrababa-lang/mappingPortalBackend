package com.slashdata.vehicleportal.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdpPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ADPMasterRepository adpMasterRepository;

    @BeforeEach
    void setUp() {
        adpMasterRepository.deleteAll();
    }

    @Test
    void uploadMasterWithJsonPayloadReplacesExistingRecords() throws Exception {
        String payload = readFromSample("samples/adp-master.json");

        mockMvc.perform(post("/api/adp/master/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].adpMakeId").value("MK1"))
            .andExpect(jsonPath("$.data[1].adpTypeId").value("TYPE2"));

        assertThat(adpMasterRepository.count()).isEqualTo(2);
        assertThat(adpMasterRepository.findAll()).extracting(ADPMaster::getMakeEnDesc)
            .containsExactlyInAnyOrder("Toyota", "Ford");
    }

    @Test
    void uploadMasterWithCsvPayloadReplacesExistingRecords() throws Exception {
        String payload = readFromSample("samples/adp-master.csv");

        mockMvc.perform(post("/api/adp/master/upload")
                .contentType("text/csv")
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].adpModelId").value("MDL1"))
            .andExpect(jsonPath("$.data[1].typeEnDesc").value("Hatchback"));

        assertThat(adpMasterRepository.count()).isEqualTo(2);
        assertThat(adpMasterRepository.findAll()).extracting(ADPMaster::getAdpTypeId)
            .containsExactlyInAnyOrder("TYPE1", "TYPE2");
    }

    private String readFromSample(String path) throws Exception {
        Path file = new ClassPathResource(path).getFile().toPath();
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
