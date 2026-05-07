package com.claimguardai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    private static final String SEEDED_USERNAME = "local.analyst";
    private static final String SEEDED_PASSWORD = "LocalPass123!";
    private static final String SEEDED_EMAIL = "local.analyst@claimguardai.local";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedCurrentUserEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    @Test
    void loginReturnsJwtForSeededUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "local.analyst",
                                  "password": "LocalPass123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void loginThenCurrentUserReturnsAuthenticatedIdentity() throws Exception {
        String accessToken = authenticateAndExtractToken();

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.username").value(SEEDED_USERNAME))
                .andExpect(jsonPath("$.email").value(SEEDED_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "local.analyst",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void loginRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body."));
    }

    private String authenticateAndExtractToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(SEEDED_USERNAME, SEEDED_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }
}
