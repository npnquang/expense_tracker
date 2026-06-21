package james.expense_tracker.service;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import james.expense_tracker.dto.auth.*;
import james.expense_tracker.model.User;
import james.expense_tracker.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRotationService tokenRotationService;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            TokenRotationService tokenRotationService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.tokenRotationService = tokenRotationService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username();

        User user = this.userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return this.tokenRotationService.createNewToken(user, request.password());
    }

    public LogoutResponse logout(LogoutRequest request) {
        String token = request.refreshToken();
        if (!this.jwtService.isRefreshToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Date now = new Date();
        Claims claims = this.jwtService.parseToken(token);
        if (now.getTime() > claims.getExpiration().getTime()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        return this.tokenRotationService.invalidateToken(token);
    }

    public RefreshResponse refresh(RefreshRequest request) {
        if (!this.jwtService.isRefreshToken(request.refreshToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Claims claims = this.jwtService.parseToken(request.refreshToken());
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String jti = claims.getId();

        // Look up user to get current role for new access token
        User user =
                this.userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED, "User not found"));

        if (!user.getId().equals(userId) || !user.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return this.tokenRotationService.refreshToken(user, jti, request.refreshToken());
    }
}
