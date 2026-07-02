package com.claimguardai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.claimguardai.claims.ClaimRepository;
import com.claimguardai.claims.ClaimReviewNoteRepository;
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
class ClaimLifecycleReviewIntegrationTest {

    private static final String SEEDED_USERNAME = "local.analyst";
    private static final String SEEDED_PASSWORD = "LocalPass123!";
    private static final String OTHER_USERNAME = "other.lifecycle.analyst";
    private static final String OTHER_PASSWORD = "OtherPass123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimReviewNoteRepository claimReviewNoteRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        claimReviewNoteRepository.deleteAll();
        claimRepository.deleteAll();

        UserAccount otherUser = userAccountRepository.findByUsernameIgnoreCase(OTHER_USERNAME)
                .orElseGet(UserAccount::new);
        otherUser.setUsername(OTHER_USERNAME);
        otherUser.setEmail("other.lifecycle.analyst@claimguardai.local");
        otherUser.setPasswordHash(passwordEncoder.encode(OTHER_PASSWORD));
        otherUser.setEnabled(true);
        otherUser.setRoles(new LinkedHashSet<>(Set.of(UserRole.REVENUE_CYCLE_ANALYST)));
        userAccountRepository.save(otherUser);
    }

    @Test
    void authenticatedUserCanUpdateOwnClaimStatus() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, uniqueClaimNumber());

        mockMvc.perform(patch("/api/claims/{claimId}/status", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_REVIEW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.id").value(claimId))
                .andExpect(jsonPath("$.claimStatus").value("IN_REVIEW"));
    }

    @Test
    void unauthenticatedUserCannotUpdateClaimStatus() throws Exception {
        mockMvc.perform(patch("/api/claims/{claimId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_REVIEW"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userCannotUpdateAnotherUsersClaimStatus() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, uniqueClaimNumber());

        mockMvc.perform(patch("/api/claims/{claimId}/status", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_REVIEW"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void authenticatedUserCanAddReviewNoteToOwnClaim() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, uniqueClaimNumber());

        mockMvc.perform(post("/api/claims/{claimId}/review-notes", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewNoteRequest("Reviewed claim details and flagged missing provider information.")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.claimId").value(claimId))
                .andExpect(jsonPath("$.noteText").value("Reviewed claim details and flagged missing provider information."))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void unauthenticatedUserCannotAddReviewNote() throws Exception {
        mockMvc.perform(post("/api/claims/{claimId}/review-notes", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewNoteRequest("Reviewed claim details.")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userCannotAddReviewNoteToAnotherUsersClaim() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, uniqueClaimNumber());

        mockMvc.perform(post("/api/claims/{claimId}/review-notes", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewNoteRequest("Reviewed claim details.")))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void authenticatedUserCanListReviewNotesForOwnClaim() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, uniqueClaimNumber());

        addReviewNote(accessToken, claimId, "First review note.");
        addReviewNote(accessToken, claimId, "Second review note.");

        mockMvc.perform(get("/api/claims/{claimId}/review-notes", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].claimId").value(claimId))
                .andExpect(jsonPath("$[0].noteText").value("First review note."))
                .andExpect(jsonPath("$[1].claimId").value(claimId))
                .andExpect(jsonPath("$[1].noteText").value("Second review note."));
    }

    @Test
    void userCannotListReviewNotesForAnotherUsersClaim() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, uniqueClaimNumber());
        addReviewNote(seededAccessToken, seededClaimId, "Owner-only review note.");

        mockMvc.perform(get("/api/claims/{claimId}/review-notes", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void invalidNoteTextFailsValidation() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, uniqueClaimNumber());

        mockMvc.perform(post("/api/claims/{claimId}/review-notes", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "noteText": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for request body."))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void invalidStatusFailsWithStructuredBadRequest() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, uniqueClaimNumber());

        mockMvc.perform(patch("/api/claims/{claimId}/status", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "NOT_A_STATUS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body."))
                .andExpect(jsonPath("$.details[0].field").value("status"))
                .andExpect(jsonPath("$.details[0].message").value("Value must match the expected type or enum value."));
    }

    private void addReviewNote(String accessToken, Long claimId, String noteText) throws Exception {
        mockMvc.perform(post("/api/claims/{claimId}/review-notes", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewNoteRequest(noteText)))
                .andExpect(status().isCreated());
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

    private String reviewNoteRequest(String noteText) {
        return """
                {
                  "noteText": "%s"
                }
                """.formatted(noteText);
    }

    private String uniqueClaimNumber() {
        return "CLM-" + UUID.randomUUID();
    }
}
