package james.expense_tracker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RefreshRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken,
        @NotBlank(message = "Username is required")
                @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
                @Pattern(
                        regexp = "^[a-zA-Z0-9_]+$",
                        message = "Username can only contain letters, numbers, and underscores")
                String username,
        @NotNull(message = "User ID is required") Long userId) {}
