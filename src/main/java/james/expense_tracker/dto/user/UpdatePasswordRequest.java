package james.expense_tracker.dto.user;

public record UpdatePasswordRequest(String newPassword, String oldPassword) {}
