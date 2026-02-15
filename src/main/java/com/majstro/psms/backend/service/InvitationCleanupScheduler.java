package com.majstro.psms.backend.service;

import com.majstro.psms.backend.service.IProjectInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to automatically expire old pending invitations.
 * Runs daily at 2 AM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationCleanupScheduler {

    private final IProjectInvitationService invitationService;

    /**
     * Runs every day at 2:00 AM to mark expired invitations.
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireOldInvitations() {
        log.info("Starting scheduled task to expire old invitations");
        try {
            invitationService.expireOldInvitations();
            log.info("Successfully completed invitation expiration task");
        } catch (Exception e) {
            log.error("Error during invitation expiration task: {}", e.getMessage(), e);
        }
    }
}

