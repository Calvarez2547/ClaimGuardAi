package com.claimguardai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerIntegrationTest.TestEndpointsConfiguration.class)
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void illegalArgumentExceptionReturnsStructuredErrorResponse() throws Exception {
        mockMvc.perform(get("/api/test/errors/illegal-argument")
                        .header("X-Correlation-Id", "test-correlation-id"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "test-correlation-id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid demo request."))
                .andExpect(jsonPath("$.path").value("/api/test/errors/illegal-argument"))
                .andExpect(jsonPath("$.correlationId").value("test-correlation-id"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @WithMockUser
    void validationErrorsReturnFieldLevelDetails() throws Exception {
        mockMvc.perform(post("/api/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for request body."))
                .andExpect(jsonPath("$.details[0].field").value("name"))
                .andExpect(jsonPath("$.details[0].message").value("Name is required."));
    }

    @TestConfiguration
    static class TestEndpointsConfiguration {

        @Bean
        TestErrorController testErrorController() {
            return new TestErrorController();
        }
    }

    @RestController
    static class TestErrorController {

        @GetMapping("/api/test/errors/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("Invalid demo request.");
        }

        @PostMapping("/api/test/errors/validation")
        void validation(@Valid @RequestBody TestValidationRequest request) {
        }
    }

    record TestValidationRequest(@NotBlank(message = "Name is required.") String name) {
    }
}
