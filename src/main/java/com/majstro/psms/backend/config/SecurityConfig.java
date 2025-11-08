package com.majstro.psms.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.majstro.psms.backend.config.UserSyncFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // ---Main Security Filter Chain ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter,
                                                   CorsConfigurationSource corsConfigurationSource, UserSyncFilter userSyncFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**","/swagger-ui/**",
                                "/swagger-ui.html","/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("APP_ADMIN")
                        .requestMatchers("/api/user/**").hasAnyAuthority("APP_ADMIN", "APP_USER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                ).addFilterAfter(userSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    // --- JWT Converter Bean (Cognito roles extraction) ---
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<String> groups = jwt.getClaimAsStringList("cognito:groups");
            if (groups == null) return Collections.emptyList();

            return groups.stream()
                    .map(SimpleGrantedAuthority::new) // keep Cognito group names as-is (APP_ADMIN, APP_USER)
                    .collect(Collectors.toList());
        });

        return converter;
    }

    // ---  Global CORS Configuration Bean ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed frontend origins
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",           // React dev server
                "https://psms.yourdomain.com"      // production domain
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allowed headers
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept"));

        // Allow credentials (tokens/cookies)
        config.setAllowCredentials(true);

        // Cache preflight result
        config.setMaxAge(3600L);

        // Apply config to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}