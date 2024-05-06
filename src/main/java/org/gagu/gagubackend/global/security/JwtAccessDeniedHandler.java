package org.gagu.gagubackend.global.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("[JwtAccessDeniedHandler] access denied");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(
                CommonResponse.builder()
                        .code(403)
                        .msg(accessDeniedException.getMessage() + "권한이 없습니다.")
                        .build()
        ));
    }
}
