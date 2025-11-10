package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IProjectInvitationService;
import com.majstro.psms.backend.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class ProjectInvitationController {

    private final IProjectInvitationService invitationService;
    private final IUserService userService;

    @PostMapping
    public ResponseEntity<?> inviteUser(
            @Valid @RequestBody InviteRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String inviterId = userService.getUserIdFromJwt(jwt);
        invitationService.sendInvitation(request.projectId(), request, inviterId);
        return ResponseEntity.ok("Invitation sent successfully to " + request.email());
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvite(
            @RequestParam String token,
            @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getClaimAsString("email");
        String message = invitationService.acceptInvitation(token, email);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectInvitationDTO>> getPendingInvitations(
            @PathVariable String projectId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = userService.getUserIdFromJwt(jwt);
        List<ProjectInvitationDTO> invitations = invitationService.getPendingInvitations(projectId);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> revokeInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = userService.getUserIdFromJwt(jwt);
        invitationService.revokeInvitation(invitationId);
        return ResponseEntity.ok("Invitation revoked successfully");
    }

    @PostMapping("/{invitationId}/resend")
    public ResponseEntity<?> resendInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = userService.getUserIdFromJwt(jwt);
        invitationService.resendInvitation(invitationId);
        return ResponseEntity.ok("Invitation resent successfully");
    }

}