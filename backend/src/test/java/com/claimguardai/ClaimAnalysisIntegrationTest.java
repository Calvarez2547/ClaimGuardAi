package com.claimguardai;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.claimguardai.analysis.ClaimAnalysisFindingRepository;
import com.claimguardai.analysis.ClaimAnalysisRepository;
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
class ClaimAnalysisIntegrationTest {

    private static final String SEEDED_USERNAME = "local.analyst";
    private static final String SEEDED_PASSWORD = "LocalPass123!";
    private static final String OTHER_USERNAME = "other.analysis.analyst";
    private static final String OTHER_PASSWORD = "OtherPass123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimAnalysisRepository claimAnalysisRepository;

    @Autowired
    private ClaimAnalysisFindingRepository claimAnalysisFindingRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        claimAnalysisFindingRepository.deleteAll();
        claimAnalysisRepository.deleteAll();
        claimRepository.deleteAll();

        UserAccount otherUser = userAccountRepository.findByUsernameIgnoreCase(OTHER_USERNAME)
                .orElseGet(UserAccount::new);
        otherUser.setUsername(OTHER_USERNAME);
        otherUser.setEmail("other.analysis.analyst@claimguardai.local");
        otherUser.setPasswordHash(passwordEncoder.encode(OTHER_PASSWORD));
        otherUser.setEnabled(true);
        otherUser.setRoles(new LinkedHashSet<>(Set.of(UserRole.REVENUE_CYCLE_ANALYST)));
        userAccountRepository.save(otherUser);
    }

    @Test
    void authenticatedUserCanAnalyzeOwnClaim() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, validClaimRequest(uniqueClaimNumber()));

        mockMvc.perform(post("/api/claims/{claimId}/analyze", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.analysisId").isNumber())
                .andExpect(jsonPath("$.claimId").value(claimId))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.riskCategory").value("LOW"))
                .andExpect(jsonPath("$.primaryRiskReason").value("No rule findings were identified."))
                .andExpect(jsonPath("$.secondaryRiskReasons.length()").value(0))
                .andExpect(jsonPath("$.findings.length()").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.baseScore").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.totalScore").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.cappedScore").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.riskCategory").value("LOW"))
                .andExpect(jsonPath("$.scoreBreakdown.primaryRiskReason").value("No rule findings were identified."))
                .andExpect(jsonPath("$.scoreBreakdown.secondaryRiskReasons.length()").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.factors.length()").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.recommendedActions[0]").value("Continue standard administrative processing."))
                .andExpect(jsonPath("$.aiSummary").value(containsString("Administrative decision support only.")))
                .andExpect(jsonPath("$.recommendedActions[0]").value("Continue standard administrative processing."))
                .andExpect(jsonPath("$.humanReviewRequired").value(false))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void unauthenticatedUserCannotAnalyzeClaim() throws Exception {
        mockMvc.perform(post("/api/claims/{claimId}/analyze", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userCannotAnalyzeAnotherUsersClaim() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, validClaimRequest(uniqueClaimNumber()));

        mockMvc.perform(post("/api/claims/{claimId}/analyze", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void authenticatedUserCanRetrieveLatestAnalysisForOwnClaim() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, validClaimRequest(uniqueClaimNumber()));

        Long firstAnalysisId = analyzeClaimAndExtractId(accessToken, claimId);
        Long secondAnalysisId = analyzeClaimAndExtractId(accessToken, claimId);

        mockMvc.perform(get("/api/claims/{claimId}/analysis/latest", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.analysisId").value(secondAnalysisId))
                .andExpect(jsonPath("$.analysisId").value(org.hamcrest.Matchers.not(firstAnalysisId)))
                .andExpect(jsonPath("$.claimId").value(claimId));
    }

    @Test
    void userCannotRetrieveLatestAnalysisForAnotherUsersClaim() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, validClaimRequest(uniqueClaimNumber()));
        analyzeClaimAndExtractId(seededAccessToken, seededClaimId);

        mockMvc.perform(get("/api/claims/{claimId}/analysis/latest", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void authenticatedUserCanRetrieveAnalysisHistoryForOwnClaim() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, validClaimRequest(uniqueClaimNumber()));

        Long firstAnalysisId = analyzeClaimAndExtractId(accessToken, claimId);
        Long secondAnalysisId = analyzeClaimAndExtractId(accessToken, claimId);

        mockMvc.perform(get("/api/claims/{claimId}/analysis/history", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].analysisId").value(secondAnalysisId))
                .andExpect(jsonPath("$[1].analysisId").value(firstAnalysisId))
                .andExpect(jsonPath("$[0].claimId").value(claimId))
                .andExpect(jsonPath("$[1].claimId").value(claimId));
    }

    @Test
    void userCannotRetrieveAnalysisHistoryForAnotherUsersClaim() throws Exception {
        String seededAccessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);
        Long seededClaimId = createClaimAndExtractId(seededAccessToken, validClaimRequest(uniqueClaimNumber()));
        analyzeClaimAndExtractId(seededAccessToken, seededClaimId);

        mockMvc.perform(get("/api/claims/{claimId}/analysis/history", seededClaimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found."));
    }

    @Test
    void analysisCreatesPersistentDatabaseRecords() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, missingPriorAuthClaimRequest(uniqueClaimNumber()));

        analyzeClaimAndExtractId(accessToken, claimId);

        org.assertj.core.api.Assertions.assertThat(claimAnalysisRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(claimAnalysisFindingRepository.count()).isEqualTo(1);
    }

    @Test
    void missingPriorAuthorizationProducesFindingAndIncreasesRisk() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, missingPriorAuthClaimRequest(uniqueClaimNumber()));

        mockMvc.perform(post("/api/claims/{claimId}/analyze", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskScore").value(45))
                .andExpect(jsonPath("$.riskCategory").value("MEDIUM"))
                .andExpect(jsonPath("$.primaryRiskReason").value("Prior authorization is required but no prior authorization number is recorded."))
                .andExpect(jsonPath("$.secondaryRiskReasons.length()").value(0))
                .andExpect(jsonPath("$.findings.length()").value(1))
                .andExpect(jsonPath("$.findings[0].findingCode").value("PRIOR_AUTH_MISSING"))
                .andExpect(jsonPath("$.findings[0].points").value(45))
                .andExpect(jsonPath("$.findings[0].category").value("PRIOR_AUTHORIZATION"))
                .andExpect(jsonPath("$.findings[0].label").value("Missing prior authorization"))
                .andExpect(jsonPath("$.findings[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.findings[0].weight").value(45))
                .andExpect(jsonPath("$.findings[0].triggered").value(true))
                .andExpect(jsonPath("$.findings[0].contribution").value(45))
                .andExpect(jsonPath("$.findings[0].recommendedAction").value("Verify whether prior authorization is required and attach or enter the authorization number before submission."))
                .andExpect(jsonPath("$.scoreBreakdown.baseScore").value(0))
                .andExpect(jsonPath("$.scoreBreakdown.totalScore").value(45))
                .andExpect(jsonPath("$.scoreBreakdown.cappedScore").value(45))
                .andExpect(jsonPath("$.scoreBreakdown.riskCategory").value("MEDIUM"))
                .andExpect(jsonPath("$.scoreBreakdown.primaryRiskReason").value("Prior authorization is required but no prior authorization number is recorded."))
                .andExpect(jsonPath("$.scoreBreakdown.factors[0].code").value("PRIOR_AUTH_MISSING"))
                .andExpect(jsonPath("$.scoreBreakdown.recommendedActions[0]").value("Verify whether prior authorization is required and attach or enter the authorization number before submission."))
                .andExpect(jsonPath("$.humanReviewRequired").value(true));
    }

    @Test
    void fallbackAiSummaryIsReturnedSafely() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, missingPriorAuthClaimRequest(uniqueClaimNumber()));

        mockMvc.perform(post("/api/claims/{claimId}/analyze", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.aiSummary").value(containsString("fallback summary is generated from backend-owned deterministic rules")))
                .andExpect(jsonPath("$.aiSummary").value(containsString("does not make final payer, legal, billing, coding, or clinical determinations")))
                .andExpect(jsonPath("$.aiSummary").value(containsString("does not override the risk score, category, findings, recommended actions, or human review decision")))
                .andExpect(jsonPath("$.recommendedActions.length()").value(2));
    }

    @Test
    void latestAnalysisResponseIncludesStructuredRiskBreakdown() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, fullyFlaggedClaimRequest(uniqueClaimNumber()));

        analyzeClaimAndExtractId(accessToken, claimId);

        mockMvc.perform(get("/api/claims/{claimId}/analysis/latest", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskScore").value(100))
                .andExpect(jsonPath("$.riskCategory").value("HIGH"))
                .andExpect(jsonPath("$.secondaryRiskReasons.length()").value(3))
                .andExpect(jsonPath("$.scoreBreakdown.totalScore").value(105))
                .andExpect(jsonPath("$.scoreBreakdown.cappedScore").value(100))
                .andExpect(jsonPath("$.scoreBreakdown.factors.length()").value(4))
                .andExpect(jsonPath("$.scoreBreakdown.recommendedActions.length()").value(5))
                .andExpect(jsonPath("$.humanReviewRequired").value(true));
    }

    private Long analyzeClaimAndExtractId(String accessToken, Long claimId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/claims/{claimId}/analyze", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("analysisId").asLong();
    }

    private Long createClaimAndExtractId(String accessToken, String requestBody) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/claims")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
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
                  "priorAuthRequired": false,
                  "claimNotes": "Initial clean claim intake documentation."
                }
                """.formatted(claimNumber);
    }

    private String missingPriorAuthClaimRequest(String claimNumber) {
        return """
                {
                  "claimNumber": "%s",
                  "patientControlNumber": "PCN-2002",
                  "payerName": "Acme Health Plan",
                  "providerName": "North Valley Clinic",
                  "serviceDate": "2026-05-01",
                  "billedAmount": 1250.75,
                  "priorAuthRequired": true,
                  "claimNotes": "Detailed administrative claim notes are available for review."
                }
                """.formatted(claimNumber);
    }

    private String fullyFlaggedClaimRequest(String claimNumber) {
        return """
                {
                  "claimNumber": "%s",
                  "payerName": "Acme Health Plan",
                  "providerName": "North Valley Clinic",
                  "serviceDate": "2026-05-01",
                  "billedAmount": 15000.00,
                  "priorAuthRequired": true,
                  "claimNotes": "Too short"
                }
                """.formatted(claimNumber);
    }

    private String uniqueClaimNumber() {
        return "CLM-" + UUID.randomUUID();
    }
}
