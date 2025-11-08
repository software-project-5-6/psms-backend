package com.majstro.psms.backend.dto;

import com.majstro.psms.backend.entity.ProjectUserRole;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String cognitoSub;
    private String email;
    private String fullName;
    private String globalRole;
    private Instant createdAt;
    private Instant updatedAt;
}