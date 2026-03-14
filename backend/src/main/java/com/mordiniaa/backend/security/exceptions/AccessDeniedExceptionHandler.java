package com.mordiniaa.backend.security.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public AccessDeniedExceptionHandler(@Qualifier("customOM") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("status", HttpServletResponse.SC_FORBIDDEN);
        responseMap.put("error", "Forbidden");
        responseMap.put("message", "You do not have permission to access this resource.");
        responseMap.put("path", request.getServletPath());
        responseMap.put("timestamp", Instant.now());

        objectMapper.writeValue(response.getOutputStream(), responseMap);
    }
}
