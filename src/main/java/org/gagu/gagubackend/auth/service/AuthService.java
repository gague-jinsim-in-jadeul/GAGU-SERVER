package org.gagu.gagubackend.auth.service;

import org.gagu.gagubackend.auth.dto.request.RequestSignDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> signIn(String authorizeCode, String type);
    ResponseEntity<?> normalSignIn(RequestSignDto requestSignDto, String type);
}
