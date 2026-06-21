package james.expense_tracker.service;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import james.expense_tracker.dto.auth.LoginResponse;
import james.expense_tracker.dto.auth.LogoutResponse;
import james.expense_tracker.dto.auth.RefreshResponse;
import james.expense_tracker.dto.user.UserInfo;
import james.expense_tracker.model.User;

@Service
public class TokenRotationService {
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public TokenRotationService(
            JwtService jwtService,
            TokenHashService tokenHashService,
            RedisService redisService,
            PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.tokenHashService = tokenHashService;
        this.redisService = redisService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse createNewToken(User user, String suppliedPassword) {
        String hashedPassword = user.getPassword();
        boolean authorized = this.passwordEncoder.matches(suppliedPassword, hashedPassword);
        if (!authorized) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String accessToken =
                this.jwtService.generateAccessToken(
                        user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken =
                this.jwtService.generateRefreshToken(user.getId(), user.getUsername());
        Claims refreshClaims = this.jwtService.parseToken(refreshToken);
        String jti = refreshClaims.getId();

        String tokenHash = this.tokenHashService.sha256(refreshToken);
        this.redisService.save(
                user.getId(), jti, tokenHash, Duration.ofMillis(this.jwtService.getRefreshExpMs()));
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                this.jwtService.getAccessExpMs() / 1000,
                this.jwtService.getRefreshExpMs() / 1000,
                new UserInfo(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    public LogoutResponse invalidateToken(String token) {
        Claims claims = this.jwtService.parseToken(token);
        Long userId = claims.get("uid", Long.class);
        String jti = claims.getId();

        if (!this.redisService.consume(userId, jti, this.tokenHashService.sha256(token))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return new LogoutResponse();
    }

    public RefreshResponse refreshToken(User user, String jti, String refreshToken) {
        Long userId = user.getId();
        String username = user.getUsername();
        String role = user.getRole().name();

        boolean consumed =
                this.redisService.consume(userId, jti, this.tokenHashService.sha256(refreshToken));
        if (!consumed) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String newAccess = this.jwtService.generateAccessToken(userId, username, role);
        String newRefresh = this.jwtService.generateRefreshToken(userId, username);

        Claims newRefreshClaims = this.jwtService.parseToken(newRefresh);
        this.redisService.save(
                userId,
                newRefreshClaims.getId(),
                tokenHashService.sha256(newRefresh),
                Duration.ofMillis(this.jwtService.getRefreshExpMs()));

        return new RefreshResponse(
                newAccess,
                newRefresh,
                "Bearer",
                this.jwtService.getAccessExpMs() / 1000,
                this.jwtService.getRefreshExpMs() / 1000);
    }
}
