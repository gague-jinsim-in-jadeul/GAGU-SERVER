package org.gagu.gagubackend.auth.service;

import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> signIn(String authorizeCode, String type);
}
