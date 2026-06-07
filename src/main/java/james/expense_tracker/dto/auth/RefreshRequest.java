package james.expense_tracker.dto.auth;

public record RefreshRequest(String refreshToken, String username, Long userId) {}
