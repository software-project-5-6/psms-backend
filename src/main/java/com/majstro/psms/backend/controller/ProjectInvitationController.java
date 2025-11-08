package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IProjectInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class ProjectInvitationController {

    private final IProjectInvitationService invitationService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> inviteUser(
            @Valid @RequestBody InviteRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String inviterId = getUserIdFromJwt(jwt);
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

        String userId = getUserIdFromJwt(jwt);
        List<ProjectInvitationDTO> invitations = invitationService.getPendingInvitations(projectId, userId);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> revokeInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = getUserIdFromJwt(jwt);
        invitationService.revokeInvitation(invitationId, userId);
        return ResponseEntity.ok("Invitation revoked successfully");
    }

    @PostMapping("/{invitationId}/resend")
    public ResponseEntity<?> resendInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = getUserIdFromJwt(jwt);
        invitationService.resendInvitation(invitationId, userId);
        return ResponseEntity.ok("Invitation resent successfully");
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<ProjectInvitationDTO> getInvitationByToken(
            @PathVariable String token) {

        ProjectInvitationDTO invitation = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(invitation);
    }

    private String getUserIdFromJwt(Jwt jwt) {
        String cognitoSub = jwt != null ? jwt.getClaimAsString("sub") : null;
        if (cognitoSub != null) {
            User user = userRepository.findByCognitoSub(cognitoSub)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return user.getId();
        }
        throw new RuntimeException("Authentication required");
    }
}