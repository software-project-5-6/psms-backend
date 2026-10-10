package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.EmailDto;
import com.majstro.psms.backend.dto.ZoomTranscriptDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.mapper.EmailMapper;
import com.majstro.psms.backend.mapper.ZoomTranscriptMapper;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.util.GoogleOAuthClient;
import com.majstro.psms.backend.util.GmailApiClient;
import com.majstro.psms.backend.util.ZoomCloudApiClient;
import com.majstro.psms.backend.util.ZoomOAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class ThirdPartyServices {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyServices.class);

    private final UserRepository userRepository;
    private final GoogleOAuthClient googleOAuthClient;
    private final GmailApiClient gmailApiClient;
    private final EmailMapper emailMapper;
    private final ZoomOAuthClient zoomOAuthClient;
    private final ZoomCloudApiClient zoomCloudApiClient;
    private final ZoomTranscriptMapper zoomTranscriptMapper;

    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${google.client.secret}")
    private String googleClientSecret;
    @Value("${google.redirect.uri}")
    private String googleRedirectUri;
    @Value("${google.scope}")
    private String googleScope;
    @Value("${google.auth.uri}")
    private String authUriGoogle;


    @Value("${zoom.client.id}")
    private String zoomClientId;
    @Value("${zoom.client.secret}")
    private String zoomClientSecret;
    @Value("${zoom.redirect.uri}")
    private String zoomRedirectUri;
    @Value("${zoom.scope}")
    private String zoomScope;
    @Value("${zoom.auth.uri}")
    private String authUriZoom;


    @Autowired
    public ThirdPartyServices(UserRepository userRepository, GoogleOAuthClient googleOAuthClient,
                              GmailApiClient gmailApiClient, EmailMapper emailMapper,
                              ZoomOAuthClient zoomOAuthClient, ZoomCloudApiClient zoomCloudApiClient,
                              ZoomTranscriptMapper zoomTranscriptMapper) {
        this.userRepository = userRepository;
        this.googleOAuthClient = googleOAuthClient;
        this.gmailApiClient = gmailApiClient;
        this.emailMapper = emailMapper;
        this.zoomOAuthClient = zoomOAuthClient;
        this.zoomCloudApiClient = zoomCloudApiClient;
        this.zoomTranscriptMapper = zoomTranscriptMapper;
    }


    // Authorize Google: build and return Google OAuth2 URL
    public String authorizeGoogle(String state) {
        String url = authUriGoogle + "?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri +
                "&response_type=code&scope=" + googleScope +
                "&access_type=offline" +
                "&prompt=consent" +
                "&state=" + java.net.URLEncoder.encode(state, java.nio.charset.StandardCharsets.UTF_8);
        return url;
    }

    /**
     * Exchanges Google authorization code for access and refresh tokens, and updates the user entity.
     */
    @Transactional
    public boolean exchangeGoogleCodeForTokens(String code, String userId) {
        if (code == null || code.isEmpty() || userId == null) {
            log.warn("Google OAuth callback missing code or userId (code present: {}, userId: {})",
                    code != null && !code.isEmpty(), userId);
            return false;
        }
        User user = userRepository.findByAuthSub(userId).orElse(null);
        if (user == null) {
            log.warn("Google OAuth callback: no user found with authSub {}", userId);
            return false;
        }
        try {
            var tokenResponse = googleOAuthClient.exchangeCodeForTokens(
                    code, googleClientId, googleClientSecret, googleRedirectUri
            );
            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            user.setGmailAccessToken(accessToken);
            if (refreshToken != null) user.setGmailRefreshToken(refreshToken);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("Failed to exchange Google authorization code for tokens: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fetches user's emails from Gmail API using a list of queries, maps to EmailDto list.
     * The stored Gmail access token expires after about an hour, so if the first attempt is
     * rejected as unauthenticated, this refreshes it using the stored refresh token and retries once.
     */
    @Transactional
    public List<EmailDto> getGmails(String userId, List<String> queries) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<java.util.Map<String, Object>> gmailMessages;
        try {
            gmailMessages = gmailApiClient.fetchEmails(user.getGmailAccessToken(), queries);
        } catch (RuntimeException e) {
            if (!isUnauthenticated(e) || user.getGmailRefreshToken() == null) {
                throw e;
            }
            String refreshedAccessToken = refreshGoogleAccessToken(user);
            gmailMessages = gmailApiClient.fetchEmails(refreshedAccessToken, queries);
        }

        return gmailMessages.stream().map(msg -> emailMapper.mapGmailMessageToEmailDto(msg)).toList();
    }

    private boolean isUnauthenticated(RuntimeException e) {
        return e.getCause() instanceof WebClientResponseException wcre
                && wcre.getStatusCode().value() == 401;
    }

    private String refreshGoogleAccessToken(User user) {
        var tokenResponse = googleOAuthClient.refreshAccessToken(
                user.getGmailRefreshToken(), googleClientId, googleClientSecret
        );
        String newAccessToken = (String) tokenResponse.get("access_token");
        user.setGmailAccessToken(newAccessToken);
        userRepository.save(user);
        return newAccessToken;
    }

    //https://zoom.us//oauth/authorize?response_type=code&client_id=kGGY73FOT2aNO2qSuA7AgA&redirect_uri=http://localhost:8080/auth/zoom/callback
    // Authorize Zoom: build and return Zoom OAuth2 URL
    public String authorizeZoom(String state) {
        String url = authUriZoom + "?response_type=code&client_id=" + zoomClientId +
                "&redirect_uri=" + zoomRedirectUri +
                "&scope=" + zoomScope +
                "&state=" + java.net.URLEncoder.encode(state, java.nio.charset.StandardCharsets.UTF_8);
        return url;
    }

    // Exchange Zoom code for tokens and store in DB
    @Transactional
    public boolean exchangeZoomCodeForTokens(String code, String userId) {
        if (code == null || code.isEmpty() || userId == null) return false;
        User user = userRepository.findByAuthSub(userId).orElse(null);
        if (user == null) return false;
        try {
            var tokenResponse = zoomOAuthClient.exchangeCodeForTokens(
                    code, zoomClientId, zoomClientSecret, zoomRedirectUri
            );
            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            user.setZoomAccessToken(accessToken);
            if (refreshToken != null) user.setZoomRefreshToken(refreshToken);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Use access token to get Zoom meeting transcripts
    public ZoomTranscriptDto getZoomTranscripts(String accessToken, String meetingId) {
        try {
            var transcriptList = zoomCloudApiClient.fetchMeetingTranscripts(meetingId, accessToken);
            return zoomTranscriptMapper.toDto(meetingId, transcriptList);
        } catch (Exception e) {
            // Optionally log the error or rethrow as a custom exception
            return null;
        }
    }
}
