package com.majstro.psms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "project_user_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "user_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectRole role;


    // Explicit setter for project to ensure the method exists even if Lombok processing is not available
    public void setProject(Project project) {
        this.project = project;
    }

    // Explicit setter for user to ensure the method exists even if Lombok processing is not available
    public void setUser(User user) {
        this.user = user;
    }
}