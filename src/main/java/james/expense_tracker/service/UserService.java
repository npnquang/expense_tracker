package james.expense_tracker.service;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import james.expense_tracker.dto.user.RegisterUserRequest;
import james.expense_tracker.dto.user.RegisterUserResponse;
import james.expense_tracker.dto.user.UpdateProfileRequest;
import james.expense_tracker.dto.user.UpdateProfileResponse;
import james.expense_tracker.dto.user.UserInfo;
import james.expense_tracker.model.User;
import james.expense_tracker.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final RedisService redisService;
    private final Map<String, BiFunction<String, Long, User>> findUserByMap;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenHashService tokenHashService,
            RedisService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenHashService = tokenHashService;
        this.redisService = redisService;
        this.findUserByMap =
                Map.of(
                        "username",
                                (value, excludeId) -> {
                                    return this.userRepository.findByUsernameAndIdNot(
                                            value, excludeId);
                                },
                        "email",
                                (value, excludeId) -> {
                                    return this.userRepository.findByEmailAndIdNot(
                                            value, excludeId);
                                });
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

        String accessToken =
                this.jwtService.generateAccessToken(
                        newUser.getId(), newUser.getUsername(), newUser.getRole().name());
        String refreshToken =
                this.jwtService.generateRefreshToken(newUser.getId(), newUser.getUsername());
        Claims refreshClaims = this.jwtService.parseToken(refreshToken);
        String jti = refreshClaims.getId();

        String tokenHash = this.tokenHashService.sha256(refreshToken);
        this.redisService.save(
                newUser.getId(),
                jti,
                tokenHash,
                Duration.ofMillis(this.jwtService.getRefreshExpMs()));

        return new RegisterUserResponse(
                newUser.getId(),
                accessToken,
                refreshToken,
                "Bearer",
                this.jwtService.getAccessExpMs() / 1000,
                this.jwtService.getRefreshExpMs() / 1000,
                new UserInfo(
                        newUser.getId(),
                        newUser.getUsername(),
                        newUser.getEmail(),
                        newUser.getRole()));
    }

    private <T> void updateField(
            T newValue,
            Long userId,
            Function<T, User> findConflict,
            Consumer<T> setter,
            String conflictMessage) {
        if (newValue == null) return;

        if (findConflict.apply(newValue) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, conflictMessage);
        }

        setter.accept(newValue);
    }

    public UpdateProfileResponse updateProfile(UpdateProfileRequest request) {
        String newEmail = request.newEmail();
        String newUsername = request.newUserName();

        if (newEmail == null && newUsername == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Both email and username field are empty");
        }

        User user =
                this.userRepository
                        .findById(request.userId())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "User not found"));

        updateField(
                request.newEmail(),
                request.userId(),
                v -> this.userRepository.findByEmailAndIdNot(v, user.getId()),
                user::setEmail,
                "Email already exists");

        updateField(
                request.newEmail(),
                request.userId(),
                v -> this.userRepository.findByEmailAndIdNot(v, user.getId()),
                user::setEmail,
                "Email already exists");

        return new UpdateProfileResponse(
                new UserInfo(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }
}
