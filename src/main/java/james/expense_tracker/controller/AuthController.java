package james.expense_tracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import james.expense_tracker.dto.auth.*;
import james.expense_tracker.service.AuthService;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/api/auth/logout")
    public LogoutResponse logout(@RequestBody LogoutRequest request) {
        return authService.logout(request);
    }

    @PostMapping("/api/auth/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
}
