package james.expense_tracker.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import james.expense_tracker.dto.user.RegisterUserRequest;
import james.expense_tracker.dto.user.RegisterUserResponse;
import james.expense_tracker.dto.user.UpdateProfileRequest;
import james.expense_tracker.dto.user.UserInfo;
import james.expense_tracker.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @PatchMapping("/profile")
    public UserInfo updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(request);
    }
}
