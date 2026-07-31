package james.expense_tracker.service;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import james.expense_tracker.dto.user.RegisterUserRequest;
import james.expense_tracker.dto.user.RegisterUserResponse;
import james.expense_tracker.dto.user.UpdateProfileRequest;
import james.expense_tracker.dto.user.UserInfo;
import james.expense_tracker.model.User;
import james.expense_tracker.repository.UserRepository;
import james.expense_tracker.utils.Utils;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final RedisService redisService;

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
    }

    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        String username = request.username();
        String password = request.password();
        String email = request.email();

        this.userRepository.findByUsername(username)
            .ifPresent(
                (u) -> new ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
            );

        this.userRepository.findByEmail(email)
            .ifPresent(
                (u) -> new ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
            );

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

    @Transactional
    public UserInfo updateProfile(UpdateProfileRequest request) {
        String newUsername = request.newUserName();

        if (newUsername == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Username field is empty");
        }

        User user =
                this.userRepository
                        .findById(request.userId())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "User not found"));
        
        if (this.userRepository.findByUsernameAndIdNot(newUsername, user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or username already exists");
        } 
        Utils.updateField(newUsername, user::setUsername);
        return new UserInfo(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // TODO: implement update email and password logic
}
