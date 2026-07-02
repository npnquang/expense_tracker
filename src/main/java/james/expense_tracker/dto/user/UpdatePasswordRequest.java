package james.expense_tracker.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotNull(message = "New password must not be empty")
                @Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters")
                @Pattern(
                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
                        message =
                                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
                String newPassword,
        @NotNull(message = "Old password must not be empty")
                @Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters")
                @Pattern(
                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
                        message =
                                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
                String oldPassword) {}
