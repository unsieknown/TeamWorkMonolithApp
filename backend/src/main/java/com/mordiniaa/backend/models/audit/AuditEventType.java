package com.mordiniaa.backend.models.audit;

public enum AuditEventType {
    REQUEST,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    REFRESH,
    REFRESH_REUSE_DETECTION,
    LOGOUT,
    ACCESS_DENIED,
    UNAUTHORIZED,
    PASSWORD_CHANGED
}
