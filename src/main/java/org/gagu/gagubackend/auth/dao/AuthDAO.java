package org.gagu.gagubackend.auth.dao;

import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.springframework.http.ResponseEntity;

public interface AuthDAO {
    ResponseEntity<?> login(RequestSaveUserDto requestSaveUserDto);
}
