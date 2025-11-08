package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IUserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSyncServiceImpl implements IUserSyncService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void ensureUserExists(Jwt jwt) {
        String sub = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String globalRole = extractGlobalRole(jwt);

        if (!userRepository.existsByCognitoSub(sub)) {
            User user = User.builder()
                    .cognitoSub(sub)
                    .email(email)
                    .fullName(name)
                    .globalRole(globalRole)
                    .build();

            userRepository.save(user);
            System.out.println("✅ New user created: " + email + " [" + globalRole + "]");
        }
    }

    private String extractGlobalRole(Jwt jwt) {
        var groups = jwt.getClaimAsStringList("cognito:groups");
        if (groups != null && !groups.isEmpty()) {
            return groups.get(0); // take first group
        }
        return "APP_USER"; // default role
    }
}