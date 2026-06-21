package com.fitlife.auth.service.impl;

import com.fitlife.auth.dto.internal.GoogleTokenPayload;
import com.fitlife.auth.service.GoogleTokenVerifierService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class GoogleTokenVerifierServiceImpl implements GoogleTokenVerifierService {

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    public GoogleTokenPayload verify(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            String providerId = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String fullName = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");

            if (email == null || email.isBlank()) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            return GoogleTokenPayload.builder()
                    .providerId(providerId)
                    .email(email)
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .emailVerified(emailVerified)
                    .build();

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Failed to verify Google ID token", exception);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}