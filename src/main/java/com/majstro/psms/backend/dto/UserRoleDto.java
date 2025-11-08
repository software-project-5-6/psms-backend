package com.majstro.psms.backend.dto;

import com.majstro.psms.backend.entity.ProjectRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDto {
    private String userId;
    private String fullName;
    private String email;
    private ProjectRole role;
}