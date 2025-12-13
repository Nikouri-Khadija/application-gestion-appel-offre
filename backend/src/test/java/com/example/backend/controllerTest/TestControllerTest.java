package com.example.backend.controllerTest;

import com.example.backend.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        TestController controller = new TestController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void adminAccess_shouldReturnAdminMessage() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bienvenue ADMIN"));
    }

    @Test
    void chefProjetAccess_shouldReturnChefMessage() throws Exception {
        mockMvc.perform(get("/api/test/chef"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bienvenue CHEF DE PROJET"));
    }

    @Test
    void consultantAccess_shouldReturnConsultantMessage() throws Exception {
        mockMvc.perform(get("/api/test/consultant"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bienvenue CONSULTANT"));
    }
}
