package com.slashdata.vehicleportal.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MakeAndModelBulkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MakeRepository makeRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @BeforeEach
    void setUp() {
        modelRepository.deleteAll();
        makeRepository.deleteAll();
        vehicleTypeRepository.deleteAll();
    }

    @Test
    void bulkCreateMakesFromJsonPayload() throws Exception {
        mockMvc.perform(post("/api/makes/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readFromSample("samples/make-bulk.json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].name").value("Toyota"))
            .andExpect(jsonPath("$.data[1].nameAr").value("فورد"));

        assertThat(makeRepository.count()).isEqualTo(2);
        assertThat(makeRepository.findAll()).extracting(Make::getName)
            .containsExactlyInAnyOrder("Toyota", "Ford");
    }

    @Test
    void bulkCreateMakesFromCsvPayload() throws Exception {
        mockMvc.perform(post("/api/makes/bulk")
                .contentType("text/csv")
                .content(readFromSample("samples/make-bulk.csv")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].nameAr").value("تويوتا"));

        assertThat(makeRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void bulkCreateModelsFromJsonPayload() throws Exception {
        Make make = createMake("Toyota");
        VehicleType type = createVehicleType("Sedan");

        String payload = readFromSample("samples/model-bulk.json")
            .replace("<MAKE_ID>", make.getId().toString())
            .replace("<TYPE_ID>", type.getId().toString());

        mockMvc.perform(post("/api/models/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].name").value("Corolla"))
            .andExpect(jsonPath("$.data[1].type.id").value(type.getId().toString()));

        assertThat(modelRepository.count()).isEqualTo(2);
        assertThat(modelRepository.findAll()).extracting(Model::getName)
            .containsExactlyInAnyOrder("Corolla", "Camry");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void bulkCreateModelsFromCsvPayload() throws Exception {
        Make make = createMake("Ford");
        VehicleType type = createVehicleType("Hatchback");

        String payload = readFromSample("samples/model-bulk.csv")
            .replace("<MAKE_ID>", make.getId().toString())
            .replace("<TYPE_ID>", type.getId().toString());

        mockMvc.perform(post("/api/models/bulk")
                .contentType("text/csv")
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].make.id").value(make.getId()))
            .andExpect(jsonPath("$.data[1].nameAr").value("كامري"));

        assertThat(modelRepository.count()).isEqualTo(2);
    }

    @Test
    void bulkCreateMakesUsesIdFromCsv() throws Exception {
        mockMvc.perform(post("/api/makes/bulk")
                .contentType("text/csv")
                .content("id,name,name_ar\n101,Tesla,تسلا"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].id").value("101"))
            .andExpect(jsonPath("$.data.records[0].name").value("Tesla"));

        assertThat(makeRepository.findById("101")).isPresent();
    }

    @Test
    void bulkCreateMakesReportsMissingField() throws Exception {
        mockMvc.perform(post("/api/makes/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"name\":\"   \"} ]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.recordsSkipped").value(1))
            .andExpect(jsonPath("$.data.skipReasons[0]").value(containsString("name")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void bulkCreateModelsReportsMissingFields() throws Exception {
        mockMvc.perform(post("/api/models/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"name\":\"\",\"makeId\":null,\"typeId\":null}]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.recordsSkipped").value(1))
            .andExpect(jsonPath("$.data.skipReasons[0]").value(
                containsString("makeId, typeId, name")));
    }

    private Make createMake(String name) {
        Make make = new Make();
        make.setId(String.valueOf(makeRepository.count() + 1));
        make.setName(name);
        return makeRepository.save(make);
    }

    private VehicleType createVehicleType(String name) {
        VehicleType type = new VehicleType();
        type.setId(vehicleTypeRepository.count() + 1);
        type.setName(name);
        return vehicleTypeRepository.save(type);
    }

    private String readFromSample(String path) throws Exception {
        Path file = new ClassPathResource(path).getFile().toPath();
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
