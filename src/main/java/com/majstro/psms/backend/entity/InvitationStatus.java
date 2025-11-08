package com.majstro.psms.backend.entity;

/**
 * Defines invitation status values.
 */
public enum InvitationStatus {
    PENDING("Pending", "Invitation sent, awaiting acceptance"),
    ACCEPTED("Accepted", "Invitation accepted and user added to project"),
    EXPIRED("Expired", "Invitation expired and no longer valid"),
    REVOKED("Revoked", "Invitation revoked by project administrator");

    private final String label;
    private final String description;

    InvitationStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}

