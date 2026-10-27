package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.service.ThirdPartyServices;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
//@RequestMapping()
@RequiredArgsConstructor
public class OAuthController {
    private final ThirdPartyServices thirdPartyServices;

    //task 1
    @GetMapping("/api/v1/oauth/google")
    public void authorizeGoogle(@RequestParam("state") String state, HttpServletResponse response) throws java.io.IOException {
        String redirectUrl = thirdPartyServices.authorizeGoogle(state);
        response.sendRedirect(redirectUrl);
    }

    //task 2
    @GetMapping("/oauth/google/callback")
    public void googleCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse response) throws java.io.IOException {
        // Assume state is formatted as <frontendUrl>::<userId>
        String[] stateParts = state.split("::", 2);
        String frontendUrl = stateParts[0];
        String userId = stateParts.length > 1 ? stateParts[1] : null;
        boolean result = false;
        if (userId != null) {
            result = thirdPartyServices.exchangeGoogleCodeForTokens(code, userId);
        }
        String redirectUrl = frontendUrl + "&result=" + result;
        response.sendRedirect(redirectUrl);
    }


    @GetMapping("/api/v1/oauth/zoom")
    public void authorizeZoom(@RequestParam("state") String state, HttpServletResponse response) throws java.io.IOException {
        String redirectUrl = thirdPartyServices.authorizeZoom(state);
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/oauth/zoom/callback")
    public void zoomCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse response) throws java.io.IOException {
        System.out.println("=== Zoom Callback Received ===");
        System.out.println("Code: " + (code != null ? "Present" : "NULL"));
        System.out.println("State: " + state);

        // Assume state is formatted as <frontendUrl>::<userId>
        String[] stateParts = state.split("::", 2);
        String frontendUrl = stateParts[0];
        String userId = stateParts.length > 1 ? stateParts[1] : null;

        System.out.println("Parsed frontendUrl: " + frontendUrl);
        System.out.println("Parsed userId: " + userId);

        boolean result = false;
        if (userId != null) {
            result = thirdPartyServices.exchangeZoomCodeForTokens(code, userId);
        } else {
            System.err.println("ERROR: userId is NULL after parsing state");
        }

        String redirectUrl = frontendUrl + "&result=" + result;
        System.out.println("Redirecting to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }


}

