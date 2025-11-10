package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;

import java.util.List;

public interface IProjectInvitationService {
    void sendInvitation(String projectId, InviteRequest request, String inviterId);
    String acceptInvitation(String token, String userEmail);
    List<ProjectInvitationDTO> getPendingInvitations(String projectId);
    void revokeInvitation(Long invitationId);
    void resendInvitation(Long invitationId);
    void expireOldInvitations();
}