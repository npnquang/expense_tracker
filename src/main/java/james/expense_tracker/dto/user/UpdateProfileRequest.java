package james.expense_tracker.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 3, max = 50, message = "New username must be between 3 and 50 characters")
                @Pattern(regexp = "^[a-zA-Z0-9_]+$")
                String newUserName,
        @NotNull(message = "User ID must not be empty") Long userId) {}
