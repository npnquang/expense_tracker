package james.expense_tracker.dto.auth;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn) {}
