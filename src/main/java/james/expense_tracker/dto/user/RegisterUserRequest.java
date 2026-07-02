package james.expense_tracker.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "Username is required")
                @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
                @Pattern(
                        regexp = "^[a-zA-Z0-9_]+$",
                        message = "Username can only contain letters, numbers, and underscores")
                String username,
        @NotBlank(message = "Password is required")
                @Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters")
                @Pattern(
                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
                        message =
                                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
                String password,
        @NotBlank(message = "Password is required")
                @Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters")
                @Pattern(
                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
                        message =
                                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
                String email) {}
