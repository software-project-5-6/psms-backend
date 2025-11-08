package com.majstro.psms.backend.service;

import org.springframework.security.oauth2.jwt.Jwt;

public interface IUserSyncService {

    void ensureUserExists(Jwt jwt);
}
