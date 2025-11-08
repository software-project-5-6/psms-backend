package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;

import java.util.List;

public interface IProjectInvitationService {
    void sendInvitation(Long projectId, InviteRequest request, Long inviterId);
    String acceptInvitation(String token, String userEmail);
    ProjectInvitationDTO getInvitationByToken(String token);
    List<ProjectInvitationDTO> getPendingInvitations(Long projectId, Long userId);
    void revokeInvitation(Long invitationId, Long userId);
    void resendInvitation(Long invitationId, Long userId);
    void expireOldInvitations();
}