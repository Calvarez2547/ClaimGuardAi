package com.claimguardai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.claimguardai.claims.ClaimRepository;
import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountRepository;
import com.claimguardai.users.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimIntakeIntegrationTest {

    private static final String SEEDED_USERNAME = "local.analyst";
    private static final String SEEDED_PASSWORD = "LocalPass123!";
    private static final String OTHER_USERNAME = "other.analyst";
    private static final String OTHER_PASSWORD = "OtherPass123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        claimRepository.deleteAll();

        UserAccount otherUser = userAccountRepository.findByUsernameIgnoreCase(OTHER_USERNAME)
                .orElseGet(UserAccount::new);
        otherUser.setUsername(OTHER_USERNAME);
        otherUser.setEmail("other.analyst@claimguardai.local");
        otherUser.setPasswordHash(passwordEncoder.encode(OTHER_PASSWORD));
        otherUser.setEnabled(true);
        otherUser.setRoles(new LinkedHashSet<>(Set.of(UserRole.REVENUE_CYCLE_ANALYST)));
        userAccountRepository.save(otherUser);
    }

    @Test
    void createClaimWhileAuthenticated() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String claimNumber = uniqueClaimNumber();

        mockMvc.perform(post("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClaimRequest(claimNumber)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.claimNumber").value(claimNumber))
                .andExpect(jsonPath("$.patientControlNumber").value("PCN-1001"))
                .andExpect(jsonPath("$.payerName").value("Acme Health Plan"))
                .andExpect(jsonPath("$.providerName").value("North Valley Clinic"))
                .andExpect(jsonPath("$.serviceDate").value("2026-05-01"))
                .andExpect(jsonPath("$.billedAmount").value(1250.75))
                .andExpect(jsonPath("$.claimStatus").value("RECEIVED"))
                .andExpect(jsonPath("$.claimNotes").value("Initial clean claim intake."))
                .andExpect(jsonPath("$.createdByUserId").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void rejectClaimCreationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClaimRequest(uniqueClaimNumber())))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    @Test
    void getCurrentUsersClaim() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, uniqueClaimNumber());

        mockMvc.perform(get("/api/claims/{claimId}", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.id").value(claimId))
                .andExpect(jsonPath("$.claimStatus").value("RECEIVED"));
    }

    @Test
    void listClaimsForCurrentUserOnly() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);

        Long firstClaimId = createClaimAndExtractId(seededAccessToken, uniqueClaimNumber());
        Long secondClaimId = createClaimAndExtractId(seededAccessToken, uniqueClaimNumber());
        createClaimAndExtractId(otherAccessToken, uniqueClaimNumber());

        mockMvc.perform(get("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + seededAccessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(secondClaimId))
                .andExpect(jsonPath("$[1].id").value(firstClaimId));
    }

    @Test
    void preventUserFromAccessingAnotherUsersClaim() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, uniqueClaimNumber());

        mockMvc.perform(get("/api/claims/{claimId}", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void validationErrorsForIncompleteClaimRequest() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);

        mockMvc.perform(post("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "claimNumber": "",
                                  "payerName": "",
                                  "providerName": "",
                                  "billedAmount": -10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for request body."))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void validationRejectsFutureServiceDate() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);

        mockMvc.perform(post("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "claimNumber": "%s",
                                  "payerName": "Acme Health Plan",
                                  "providerName": "North Valley Clinic",
                                  "serviceDate": "2099-01-01",
                                  "billedAmount": 1250.75
                                }
                                """.formatted(uniqueClaimNumber())))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for request body."))
                .andExpect(jsonPath("$.details[0].field").value("serviceDate"))
                .andExpect(jsonPath("$.details[0].message").value("Service date cannot be in the future."));
    }

    @Test
    void unknownRequestFieldsFailWithStructuredBadRequest() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);

        mockMvc.perform(post("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "claimNumber": "%s",
                                  "payerName": "Acme Health Plan",
                                  "providerName": "North Valley Clinic",
                                  "serviceDate": "2026-05-01",
                                  "billedAmount": 1250.75,
                                  "unexpectedField": "should-not-bind"
                                }
                                """.formatted(uniqueClaimNumber())))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body."))
                .andExpect(jsonPath("$.details[0].field").value("unexpectedField"))
                .andExpect(jsonPath("$.details[0].message").value("Unknown field is not allowed."));
    }

    private Long createClaimAndExtractId(String accessToken, String claimNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClaimRequest(claimNumber)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private String authenticateAndExtractToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private String validClaimRequest(String claimNumber) {
        return """
                {
                  "claimNumber": "%s",
                  "patientControlNumber": "PCN-1001",
                  "payerName": "Acme Health Plan",
                  "providerName": "North Valley Clinic",
                  "serviceDate": "2026-05-01",
                  "billedAmount": 1250.75,
                  "claimNotes": "Initial clean claim intake."
                }
                """.formatted(claimNumber);
    }

    private String uniqueClaimNumber() {
        return "CLM-" + UUID.randomUUID();
    }
}
