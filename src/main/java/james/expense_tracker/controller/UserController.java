package james.expense_tracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import james.expense_tracker.dto.user.RegisterUserRequest;
import james.expense_tracker.dto.user.RegisterUserResponse;
import james.expense_tracker.dto.user.UpdateProfileRequest;
import james.expense_tracker.dto.user.UpdateProfileResponse;
import james.expense_tracker.service.UserService;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/user/register")
    public RegisterUserResponse register(@RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("api/user/profile")
    public UpdateProfileResponse updateProfile(@RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(request);
    }
}
