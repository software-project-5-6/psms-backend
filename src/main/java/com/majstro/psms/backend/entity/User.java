package com.majstro.psms.backend.entity;

import com.majstro.psms.backend.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(length = 4, nullable = false)
    private String id;

    @Column(name = "cognito_sub", nullable = false, unique = true, length = 50)
    private String cognitoSub;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "global_role", length = 50)
    private String globalRole = "APP_USER";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ProjectUserRole> projectRoles = new HashSet<>();

    public void addProjectRole(ProjectUserRole pur) {
        projectRoles.add(pur);
        pur.setUser(this);
    }

    public void removeProjectRole(ProjectUserRole pur) {
        projectRoles.remove(pur);
        pur.setUser(null);
    }

    /**
     * JPA lifecycle callback to generate custom ID before persisting
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = IdGenerator.generateIdWithPrefix("U"); // U for User (e.g., UA12, UX45)
        }
    }
}