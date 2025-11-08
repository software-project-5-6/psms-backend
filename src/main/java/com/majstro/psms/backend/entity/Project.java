package com.majstro.psms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String projectName;

    @Column(length = 500)
    private String description;

    @Column(name = "client_name", length = 150)
    private String clientName;

    @Column(name = "client_email", length = 150)
    private String clientEmail;

    @Column(name = "client_phone", length = 150)
    private String clientPhone;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "price")
    private Double price;


    @Column(name = "artifact_count")
    private Integer artifactCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ProjectUserRole> userRoles = new HashSet<>();


    // @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Artifact> artifacts;


    //Helpler methods
    public void addUserRole(ProjectUserRole projectUserRole) {
        userRoles.add(projectUserRole);
        projectUserRole.setProject(this);
    }

    public void removeUserRole(ProjectUserRole projectUserRole) {
        userRoles.remove(projectUserRole);
        projectUserRole.setProject(null);
    }
}