package james.expense_tracker.dto.user;

public record UpdateProfileRequest(String newUserName, String newEmail, Long userId) {}
