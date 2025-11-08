package com.majstro.psms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // Will store: ADMIN, MANAGER, CONTRIBUTOR, VIEWER

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String status; // Will store: PENDING, ACCEPTED, EXPIRED, REVOKED

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "invited_by", length = 4)
    private String invitedBy;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isAccepted() {
        return "ACCEPTED".equalsIgnoreCase(status);
    }
}