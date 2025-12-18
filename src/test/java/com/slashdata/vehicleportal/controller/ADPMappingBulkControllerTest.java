package com.slashdata.vehicleportal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.Role;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ADPMappingBulkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ADPMasterRepository adpMasterRepository;

    @Autowired
    private ADPMappingRepository adpMappingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        adpMappingRepository.deleteAll();
        adpMasterRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "reviewer@example.com", roles = "MAPPING_ADMIN")
    void bulkActionEndpointIsAvailable() throws Exception {
        User reviewer = new User();
        reviewer.setEmail("reviewer@example.com");
        reviewer.setPassword("secret");
        reviewer.setRole(Role.MAPPING_ADMIN);
        userRepository.save(reviewer);

        ADPMaster adpMaster = new ADPMaster();
        adpMaster.setAdpMakeId("123");
        adpMaster.setMakeEnDesc("Toyota");
        adpMaster.setAdpModelId("456");
        adpMaster.setModelEnDesc("Corolla");
        adpMasterRepository.save(adpMaster);

        ADPMapping mapping = new ADPMapping();
        mapping.setAdpMaster(adpMaster);
        mapping.setUpdatedBy(reviewer);
        ADPMapping savedMapping = adpMappingRepository.save(mapping);

        String payload = "{\"action\":\"APPROVE\",\"ids\":[\"" + savedMapping.getId() + "\"]}";

        mockMvc.perform(post("/api/adp/mappings/bulk-action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("OK"));
    }
}
