package com.claimguardai.claims;

public class ClaimNotFoundException extends RuntimeException {

    public ClaimNotFoundException() {
        super("Claim not found.");
    }
}
