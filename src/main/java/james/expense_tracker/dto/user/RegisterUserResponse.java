package james.expense_tracker.dto.user;

public record RegisterUserResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UserInfo userInfo) {}
