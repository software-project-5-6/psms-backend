package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;

import java.util.List;

public interface IProjectInvitationService {
    void sendInvitation(String projectId, InviteRequest request, String inviterId);
    String acceptInvitation(String token, String userEmail);
    ProjectInvitationDTO getInvitationByToken(String token);
    List<ProjectInvitationDTO> getPendingInvitations(String projectId, String userId);
    void revokeInvitation(Long invitationId, String userId);
    void resendInvitation(Long invitationId, String userId);
    void expireOldInvitations();
}