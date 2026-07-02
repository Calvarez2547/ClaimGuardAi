package com.claimguardai.claims;

import com.claimguardai.users.UserAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoClaimSeeder {

    private static final String[] PAYERS = {
        "Blue Cross Blue Shield", "Aetna", "UnitedHealthcare", "Cigna", "Humana",
        "Medicare", "Medicaid", "Anthem", "Centene", "Molina Healthcare"
    };

    private static final String[] PROVIDERS = {
        "Riverside Medical Center", "Summit Health Partners", "Valley Regional Hospital",
        "Oakdale Physician Group", "Northside Surgical Associates", "Lakewood Family Practice",
        "Metro Orthopedic Clinic", "Coastal Radiology Group", "Pinecrest Urgent Care",
        "Highpoint Cardiology Associates"
    };

    private static final String[] DENIED_NOTES = {
        "Claim denied: service not covered under current plan benefit.",
        "Authorization not obtained prior to service. Retroactive auth request pending.",
        "Duplicate claim submission detected — original processed on prior date.",
        "Member not eligible on date of service. Coverage verification failed.",
        "Diagnosis code does not support medical necessity for billed procedure.",
        "Claim denied: out-of-network provider, no referral on file.",
        "Timely filing limit exceeded. Claim received beyond 90-day submission window.",
    };

    private static final String[] NEEDS_INFO_NOTES = {
        "Additional documentation required: operative report for billed procedure.",
        "Medical records requested to support inpatient admission criteria.",
        "Itemized bill required — charges must be broken down by revenue code.",
        "Coordination of benefits information needed; patient reports secondary coverage.",
        "Referring provider NPI missing from claim — resubmission required.",
        "Clinical notes for date of service must be submitted within 30 days.",
    };

    private static final String[] REVIEW_NOTES = {
        "Claim under clinical review for medical necessity determination.",
        "High-dollar claim routed for senior reviewer approval.",
        "Concurrent review in progress — authorization decision pending.",
        "Claim flagged for fraud and abuse screening per plan protocol.",
        "Complex procedure under secondary review; estimated decision in 5–7 business days.",
    };

    private static final String[] APPROVED_NOTES = {
        "Claim approved and remittance issued per plan allowable.",
        "Paid at in-network contracted rate. ERA transmitted to provider.",
        "Approved with adjustment: deductible applied per member benefits.",
        "Processed and paid. COB applied; secondary payer notified.",
    };

    // Weighted status pool — higher risk statuses appear more frequently
    private static final ClaimStatus[] WEIGHTED_STATUSES = {
        ClaimStatus.DENIED,     ClaimStatus.DENIED,     ClaimStatus.DENIED,     ClaimStatus.DENIED,
        ClaimStatus.DENIED,     ClaimStatus.DENIED,     ClaimStatus.DENIED,     ClaimStatus.DENIED,
        ClaimStatus.NEEDS_INFO, ClaimStatus.NEEDS_INFO, ClaimStatus.NEEDS_INFO, ClaimStatus.NEEDS_INFO,
        ClaimStatus.NEEDS_INFO, ClaimStatus.NEEDS_INFO,
        ClaimStatus.IN_REVIEW,  ClaimStatus.IN_REVIEW,  ClaimStatus.IN_REVIEW,  ClaimStatus.IN_REVIEW,
        ClaimStatus.SUBMITTED,  ClaimStatus.SUBMITTED,  ClaimStatus.SUBMITTED,
        ClaimStatus.RECEIVED,   ClaimStatus.RECEIVED,
        ClaimStatus.APPROVED,   ClaimStatus.APPROVED,
        ClaimStatus.CLOSED,
    };

    private final ClaimRepository claimRepository;

    public DemoClaimSeeder(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional
    public void seedForUser(UserAccount user) {
        if (claimRepository.countByCreatedById(user.getId()) > 0) {
            return; // already seeded
        }

        Random rng = new Random(user.getId()); // deterministic per user so reruns are stable
        List<Claim> claims = new ArrayList<>(30);

        for (int i = 0; i < 30; i++) {
            ClaimStatus status = WEIGHTED_STATUSES[rng.nextInt(WEIGHTED_STATUSES.length)];
            claims.add(buildClaim(user, status, i + 1, rng));
        }

        claimRepository.saveAll(claims);
    }

    private Claim buildClaim(UserAccount user, ClaimStatus status, int seq, Random rng) {
        Claim c = new Claim();

        LocalDate serviceDate = LocalDate.now()
                .minusDays(rng.nextInt(365) + 1L);

        String claimNumber = String.format("CLM-%s-%05d",
                serviceDate.getYear(), (user.getId() * 100L + seq) % 99999);

        c.setClaimNumber(claimNumber);
        c.setPatientControlNumber(String.format("PCN-%06d", rng.nextInt(999999)));
        c.setPayerName(PAYERS[rng.nextInt(PAYERS.length)]);
        c.setProviderName(PROVIDERS[rng.nextInt(PROVIDERS.length)]);
        c.setServiceDate(serviceDate);
        c.setBilledAmount(billedAmount(status, rng));
        c.setClaimStatus(status);
        c.setCreatedBy(user);

        boolean needsAuth = status == ClaimStatus.DENIED || rng.nextInt(10) < 4;
        c.setPriorAuthRequired(needsAuth);
        if (needsAuth && status != ClaimStatus.DENIED && rng.nextBoolean()) {
            c.setPriorAuthNumber(String.format("AUTH-%07d", rng.nextInt(9999999)));
        }

        c.setClaimNotes(pickNote(status, rng));
        return c;
    }

    private BigDecimal billedAmount(ClaimStatus status, Random rng) {
        int cents = switch (status) {
            case DENIED     -> 500_00 + rng.nextInt(4500_00);   // $500–$5,000
            case NEEDS_INFO -> 1000_00 + rng.nextInt(9000_00);  // $1,000–$10,000
            case IN_REVIEW  -> 2000_00 + rng.nextInt(18000_00); // $2,000–$20,000
            case SUBMITTED  -> 800_00 + rng.nextInt(7200_00);   // $800–$8,000
            case APPROVED   -> 300_00 + rng.nextInt(2700_00);   // $300–$3,000
            default         -> 200_00 + rng.nextInt(4800_00);   // $200–$5,000
        };
        return BigDecimal.valueOf(cents).movePointLeft(2);
    }

    private String pickNote(ClaimStatus status, Random rng) {
        return switch (status) {
            case DENIED     -> DENIED_NOTES[rng.nextInt(DENIED_NOTES.length)];
            case NEEDS_INFO -> NEEDS_INFO_NOTES[rng.nextInt(NEEDS_INFO_NOTES.length)];
            case IN_REVIEW  -> REVIEW_NOTES[rng.nextInt(REVIEW_NOTES.length)];
            case APPROVED   -> APPROVED_NOTES[rng.nextInt(APPROVED_NOTES.length)];
            default         -> null;
        };
    }
}
