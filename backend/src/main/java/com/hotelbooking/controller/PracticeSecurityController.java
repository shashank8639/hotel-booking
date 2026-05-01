package com.hotelbooking.controller;

import com.hotelbooking.security.AdminOnlyDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Thin HTTP surface for method-security practice ({@code @PreAuthorize} on the service).
 */
@RestController
@RequestMapping("/practice/security")
@RequiredArgsConstructor
public class PracticeSecurityController {

    private final AdminOnlyDemoService adminOnlyDemoService;

    @GetMapping("/admin-ping")
    public ResponseEntity<Map<String, String>> adminPing() {
        return ResponseEntity.ok(Map.of("result", adminOnlyDemoService.adminOnlyPing()));
    }

    @GetMapping("/auth-ping")
    public ResponseEntity<Map<String, String>> authPing() {
        return ResponseEntity.ok(Map.of("result", adminOnlyDemoService.anyAuthenticatedPing()));
    }
}
