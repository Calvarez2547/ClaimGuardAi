package com.claimguardai.audit;

public enum AuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    REGISTER,
    TOKEN_REFRESHED,
    CLAIM_CREATED,
    CLAIM_STATUS_UPDATED,
    REVIEW_NOTE_ADDED,
    ANALYSIS_RUN,
    UNAUTHORIZED_ACCESS
}
