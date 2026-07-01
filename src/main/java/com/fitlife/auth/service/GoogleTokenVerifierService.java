package com.fitlife.auth.service;

import com.fitlife.auth.dto.internal.GoogleTokenPayload;

public interface GoogleTokenVerifierService {

    GoogleTokenPayload verify(String idToken);
}