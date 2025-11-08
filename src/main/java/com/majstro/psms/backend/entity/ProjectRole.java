package com.majstro.psms.backend.entity;

/**
 * Defines per-project roles.
 * Controls permission levels within a project space.
 */
public enum ProjectRole {

    ADMIN("Administrator", "Full control over the project"),
    MANAGER("Manager", "Manage contributors and project workflow"),
    CONTRIBUTOR("Contributor", "Can add and edit artifacts"),
    VIEWER("Viewer", "Read-only access");

    private final String label;
    private final String description;

    ProjectRole(String label, String description) {
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