package james.expense_tracker.dto.auth;

import james.expense_tracker.dto.user.UserInfo;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UserInfo userInfo) {}
