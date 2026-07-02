package com.claimguardai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.claimguardai.analysis.ClaimAnalysisFindingRepository;
import com.claimguardai.analysis.ClaimAnalysisRepository;
import com.claimguardai.claims.Claim;
import com.claimguardai.claims.ClaimRepository;
import com.claimguardai.claims.ClaimReviewNoteRepository;
import com.claimguardai.claims.ClaimStatus;
import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountRepository;
import com.claimguardai.users.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class DashboardIntegrationTest {

    private static final String SEEDED_USERNAME = "local.analyst";
    private static final String SEEDED_PASSWORD = "LocalPass123!";
    private static final String OTHER_USERNAME = "other.dashboard.analyst";
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
    private ClaimReviewNoteRepository claimReviewNoteRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        claimAnalysisFindingRepository.deleteAll();
        claimAnalysisRepository.deleteAll();
        claimReviewNoteRepository.deleteAll();
        claimRepository.deleteAll();

        UserAccount otherUser = userAccountRepository.findByUsernameIgnoreCase(OTHER_USERNAME)
                .orElseGet(UserAccount::new);
        otherUser.setUsername(OTHER_USERNAME);
        otherUser.setEmail("other.dashboard.analyst@claimguardai.local");
        otherUser.setPasswordHash(passwordEncoder.encode(OTHER_PASSWORD));
        otherUser.setEnabled(true);
        otherUser.setRoles(new LinkedHashSet<>(Set.of(UserRole.REVENUE_CYCLE_ANALYST)));
        userAccountRepository.save(otherUser);
    }

    @Test
    void authenticatedUserCanRetrieveOwnerScopedDashboardSummary() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        String otherAccessToken = authenticateAndExtractToken(OTHER_USERNAME, OTHER_PASSWORD);

        Long claimAId = createClaimAndExtractId(accessToken, fullyFlaggedClaimRequest(uniqueClaimNumber()));
        updateClaimStatus(claimAId, ClaimStatus.APPROVED);
        analyzeClaimAndExtractId(accessToken, claimAId);

        Claim claimA = claimRepository.findById(claimAId).orElseThrow();
        claimA.setPatientControlNumber("PCN-CLEAN-1001");
        claimA.setPriorAuthNumber("PA-9001");
        claimA.setClaimNotes("Detailed administrative notes are present and support standard processing.");
        claimA.setBilledAmount(new BigDecimal("1250.75"));
        claimRepository.save(claimA);
        Long claimALatestAnalysisId = analyzeClaimAndExtractId(accessToken, claimAId);

        Long claimBId = createClaimAndExtractId(accessToken, missingPriorAuthClaimRequest(uniqueClaimNumber()));
        updateClaimStatus(claimBId, ClaimStatus.DENIED);
        Long claimBAnalysisId = analyzeClaimAndExtractId(accessToken, claimBId);

        Long claimCId = createClaimAndExtractId(accessToken, cleanClaimRequest(uniqueClaimNumber()));
        updateClaimStatus(claimCId, ClaimStatus.SUBMITTED);

        Long otherClaimId = createClaimAndExtractId(otherAccessToken, fullyFlaggedClaimRequest(uniqueClaimNumber()));
        analyzeClaimAndExtractId(otherAccessToken, otherClaimId);

        mockMvc.perform(get("/api/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.totalClaims").value(3))
                .andExpect(jsonPath("$.claimsByStatus.length()").value(3))
                .andExpect(jsonPath("$.claimsByStatus[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$.claimsByStatus[0].count").value(1))
                .andExpect(jsonPath("$.claimsByStatus[1].status").value("APPROVED"))
                .andExpect(jsonPath("$.claimsByStatus[1].count").value(1))
                .andExpect(jsonPath("$.claimsByStatus[2].status").value("DENIED"))
                .andExpect(jsonPath("$.claimsByStatus[2].count").value(1))
                .andExpect(jsonPath("$.analysesByRiskCategory.length()").value(2))
                .andExpect(jsonPath("$.analysesByRiskCategory[0].riskCategory").value("LOW"))
                .andExpect(jsonPath("$.analysesByRiskCategory[0].count").value(1))
                .andExpect(jsonPath("$.analysesByRiskCategory[1].riskCategory").value("MEDIUM"))
                .andExpect(jsonPath("$.analysesByRiskCategory[1].count").value(1))
                .andExpect(jsonPath("$.humanReviewRequiredCount").value(1))
                .andExpect(jsonPath("$.lowRiskCount").value(1))
                .andExpect(jsonPath("$.mediumRiskCount").value(1))
                .andExpect(jsonPath("$.highRiskCount").value(0))
                .andExpect(jsonPath("$.recentClaims.length()").value(3))
                .andExpect(jsonPath("$.recentClaims[0].claimId").value(claimCId))
                .andExpect(jsonPath("$.recentClaims[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$.recentClaims[1].claimId").value(claimBId))
                .andExpect(jsonPath("$.recentClaims[1].latestRiskScore").value(45))
                .andExpect(jsonPath("$.recentClaims[1].latestRiskCategory").value("MEDIUM"))
                .andExpect(jsonPath("$.recentClaims[1].humanReviewRequired").value(true))
                .andExpect(jsonPath("$.recentClaims[2].claimId").value(claimAId))
                .andExpect(jsonPath("$.recentClaims[2].latestRiskScore").value(0))
                .andExpect(jsonPath("$.recentClaims[2].latestRiskCategory").value("LOW"))
                .andExpect(jsonPath("$.recentClaims[2].humanReviewRequired").value(false))
                .andExpect(jsonPath("$.recentAnalyses.length()").value(2))
                .andExpect(jsonPath("$.recentAnalyses[0].analysisId").value(claimBAnalysisId))
                .andExpect(jsonPath("$.recentAnalyses[0].claimId").value(claimBId))
                .andExpect(jsonPath("$.recentAnalyses[1].analysisId").value(claimALatestAnalysisId))
                .andExpect(jsonPath("$.recentAnalyses[1].claimId").value(claimAId))
                .andExpect(jsonPath("$.highestRiskClaims.length()").value(2))
                .andExpect(jsonPath("$.highestRiskClaims[0].claimId").value(claimBId))
                .andExpect(jsonPath("$.highestRiskClaims[0].latestRiskScore").value(45))
                .andExpect(jsonPath("$.highestRiskClaims[0].latestRiskCategory").value("MEDIUM"))
                .andExpect(jsonPath("$.highestRiskClaims[1].claimId").value(claimAId))
                .andExpect(jsonPath("$.highestRiskClaims[1].latestRiskScore").value(0))
                .andExpect(jsonPath("$.topRiskFactors.length()").value(1))
                .andExpect(jsonPath("$.topRiskFactors[0].code").value("PRIOR_AUTH_MISSING"))
                .andExpect(jsonPath("$.topRiskFactors[0].category").value("PRIOR_AUTHORIZATION"))
                .andExpect(jsonPath("$.topRiskFactors[0].label").value("Missing prior authorization"))
                .andExpect(jsonPath("$.topRiskFactors[0].count").value(1))
                .andExpect(jsonPath("$.topRiskFactors[0].totalContribution").value(45))
                .andExpect(jsonPath("$.generatedAt").isNotEmpty());
    }

    @Test
    void unauthenticatedUserCannotRetrieveDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    @Test
    void emptyDashboardStateReturnsZeroCountsAndEmptyLists() throws Exception {
        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);

        mockMvc.perform(get("/api/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.totalClaims").value(0))
                .andExpect(jsonPath("$.claimsByStatus.length()").value(0))
                .andExpect(jsonPath("$.analysesByRiskCategory.length()").value(0))
                .andExpect(jsonPath("$.humanReviewRequiredCount").value(0))
                .andExpect(jsonPath("$.lowRiskCount").value(0))
                .andExpect(jsonPath("$.mediumRiskCount").value(0))
                .andExpect(jsonPath("$.highRiskCount").value(0))
                .andExpect(jsonPath("$.recentClaims.length()").value(0))
                .andExpect(jsonPath("$.recentAnalyses.length()").value(0))
                .andExpect(jsonPath("$.highestRiskClaims.length()").value(0))
                .andExpect(jsonPath("$.topRiskFactors.length()").value(0))
                .andExpect(jsonPath("$.generatedAt").isNotEmpty());
    }

    private void updateClaimStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepository.findById(claimId).orElseThrow();
        claim.setClaimStatus(status);
        claimRepository.save(claim);
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

    private String cleanClaimRequest(String claimNumber) {
        return """
                {
                  "claimNumber": "%s",
                  "patientControlNumber": "PCN-1001",
                  "payerName": "Acme Health Plan",
                  "providerName": "North Valley Clinic",
                  "serviceDate": "2026-05-01",
                  "billedAmount": 1250.75,
                  "priorAuthRequired": false,
                  "claimNotes": "Initial clean claim intake documentation with sufficient administrative detail."
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
