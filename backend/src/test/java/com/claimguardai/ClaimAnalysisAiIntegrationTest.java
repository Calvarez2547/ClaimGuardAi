package com.claimguardai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.claimguardai.ai.AiProviderException;
import com.claimguardai.ai.AiProviderResponse;
import com.claimguardai.ai.AiProviderType;
import com.claimguardai.ai.OpenAiProviderClient;
import com.claimguardai.analysis.ClaimAnalysis;
import com.claimguardai.analysis.ClaimAnalysisFindingRepository;
import com.claimguardai.analysis.ClaimAnalysisRepository;
import com.claimguardai.claims.ClaimRepository;
import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountRepository;
import com.claimguardai.users.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "app.ai.enabled=true",
        "app.ai.provider=OPENAI",
        "app.ai.api-key=test-api-key",
        "app.ai.model=gpt-4o-mini",
        "app.ai.timeout-seconds=5"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimAnalysisAiIntegrationTest {

    private static final String SEEDED_USERNAME = "local.analyst";
    private static final String SEEDED_PASSWORD = "LocalPass123!";
    private static final String OTHER_USERNAME = "other.ai.analyst";
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

    @MockBean
    private OpenAiProviderClient openAiProviderClient;

    @BeforeEach
    void setUp() {
        claimAnalysisFindingRepository.deleteAll();
        claimAnalysisRepository.deleteAll();
        claimRepository.deleteAll();

        UserAccount otherUser = userAccountRepository.findByUsernameIgnoreCase(OTHER_USERNAME)
                .orElseGet(UserAccount::new);
        otherUser.setUsername(OTHER_USERNAME);
        otherUser.setEmail("other.ai.analyst@claimguardai.local");
        otherUser.setPasswordHash(passwordEncoder.encode(OTHER_PASSWORD));
        otherUser.setEnabled(true);
        otherUser.setRoles(new LinkedHashSet<>(Set.of(UserRole.REVENUE_CYCLE_ANALYST)));
        userAccountRepository.save(otherUser);

        given(openAiProviderClient.supportedProvider()).willReturn(AiProviderType.OPENAI);
    }

    @Test
    void aiProviderSuccessPersistsStructuredOutputAndDisablesFallback() throws Exception {
        given(openAiProviderClient.analyze(any())).willReturn(new AiProviderResponse(
                "The claim should be reviewed for missing prior authorization details.",
                "The deterministic score is elevated because prior authorization is required but not recorded.",
                List.of("Prior authorization details are missing from the claim record."),
                List.of("Confirm the prior authorization number with the ordering workflow."),
                "HIGH",
                "This is AI-assisted review support and not a final payer decision."));

        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, missingPriorAuthClaimRequest(uniqueClaimNumber()));

        mockMvc.perform(post("/api/claims/{claimId}/analyze", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.fallbackUsed").value(false))
                .andExpect(jsonPath("$.riskScore").value(45))
                .andExpect(jsonPath("$.recommendedActions[0]").value("Verify whether prior authorization is required and attach or enter the authorization number before submission."))
                .andExpect(jsonPath("$.aiSummary").value(org.hamcrest.Matchers.containsString("AI-assisted reviewer support only.")))
                .andExpect(jsonPath("$.aiSummary").value(org.hamcrest.Matchers.containsString("Review priority: HIGH")))
                .andExpect(jsonPath("$.aiSummary").value(org.hamcrest.Matchers.containsString("final payer decision")));

        ClaimAnalysis persistedAnalysis = claimAnalysisRepository.findAll().getFirst();
        assertThat(persistedAnalysis.isFallbackUsed()).isFalse();
        assertThat(persistedAnalysis.getAiStructuredOutput()).contains("\"reviewPriority\":\"HIGH\"");
    }

    @Test
    void aiProviderFailureFallsBackSafelyWithoutBreakingAnalysis() throws Exception {
        given(openAiProviderClient.analyze(any()))
                .willThrow(new AiProviderException("Simulated provider failure."));

        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, missingPriorAuthClaimRequest(uniqueClaimNumber()));

        mockMvc.perform(post("/api/claims/{claimId}/analyze", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.aiSummary").value(org.hamcrest.Matchers.containsString("fallback summary is generated from backend-owned deterministic rules")))
                .andExpect(jsonPath("$.riskScore").value(45));
    }

    @Test
    void latestAndHistoryStillWorkWhenAiProviderSucceeds() throws Exception {
        given(openAiProviderClient.analyze(any()))
                .willReturn(new AiProviderResponse(
                        "First provider summary.",
                        "First risk explanation.",
                        List.of("First documentation concern."),
                        List.of("First suggested action."),
                        "MEDIUM",
                        "This is AI-assisted review support and not a final payer decision."))
                .willReturn(new AiProviderResponse(
                        "Second provider summary.",
                        "Second risk explanation.",
                        List.of(),
                        List.of("Second suggested action."),
                        "MEDIUM",
                        "This is AI-assisted review support and not a final payer decision."));

        String accessToken = authenticateAndExtractToken(SEEDED_USERNAME, SEEDED_PASSWORD);
        Long claimId = createClaimAndExtractId(accessToken, validClaimRequest(uniqueClaimNumber()));

        Long firstAnalysisId = analyzeClaimAndExtractId(accessToken, claimId);
        Long secondAnalysisId = analyzeClaimAndExtractId(accessToken, claimId);

        mockMvc.perform(get("/api/claims/{claimId}/analysis/latest", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(secondAnalysisId))
                .andExpect(jsonPath("$.fallbackUsed").value(false))
                .andExpect(jsonPath("$.aiSummary").value(org.hamcrest.Matchers.containsString("Second provider summary.")));

        mockMvc.perform(get("/api/claims/{claimId}/analysis/history", claimId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].analysisId").value(secondAnalysisId))
                .andExpect(jsonPath("$[0].fallbackUsed").value(false))
                .andExpect(jsonPath("$[1].analysisId").value(firstAnalysisId))
                .andExpect(jsonPath("$[1].fallbackUsed").value(false));
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

    private String uniqueClaimNumber() {
        return "CLM-" + UUID.randomUUID();
    }
}
