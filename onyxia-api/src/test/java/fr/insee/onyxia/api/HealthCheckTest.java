package fr.insee.onyxia.api;

import fr.insee.onyxia.api.configuration.BaseTest;
import fr.insee.onyxia.api.controller.pub.HealthCheck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheck.class)
public class HealthCheckTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/public/healthcheck")).andExpect(status().isOk())
                .andDo(document("healthcheck"));
    }
    
}
