package james.expense_tracker.service;

import java.time.Duration;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import james.expense_tracker.dto.auth.*;
import james.expense_tracker.dto.user.RegisterUserRequest;
import james.expense_tracker.dto.user.RegisterUserResponse;
import james.expense_tracker.dto.user.UserInfo;
import james.expense_tracker.model.User;
import james.expense_tracker.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final TokenHashService tokenHashService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RedisService redisService,
            TokenHashService tokenHashService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisService = redisService;
        this.tokenHashService = tokenHashService;
    }

    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        String username = request.username();
        String password = request.password();
        String email = request.email();

        User user = this.userRepository.findByUsername(username);
        if (user != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        user = this.userRepository.findByEmail(email);
        if (user != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // this automatically adds the salt to the password
        // and hashes it
        String hashedPassword = this.passwordEncoder.encode(password);

        User newUser = new User(username, hashedPassword, email);
        newUser = this.userRepository.save(newUser);
        return new RegisterUserResponse(newUser.getId());
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        User user = this.userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String hashedPassword = user.getPassword();
        boolean authorized = this.passwordEncoder.matches(password, hashedPassword);
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

        Long userId = claims.get("uid", Long.class);
        String jti = claims.getId();

        if (!this.redisService.consume(userId, jti, this.tokenHashService.sha256(token))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return new LogoutResponse();
    }

    public RefreshResponse refresh(RefreshRequest request) {
        if (!this.jwtService.isRefreshToken(request.refreshToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Claims claims = this.jwtService.parseToken(request.refreshToken());
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String jti = claims.getId();

        boolean consumed =
                this.redisService.consume(
                        userId, jti, this.tokenHashService.sha256(request.refreshToken()));
        if (!consumed) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        // Look up user to get current role for new access token
        User user =
                this.userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED, "User not found"));

        String newAccess =
                this.jwtService.generateAccessToken(userId, username, user.getRole().name());
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
