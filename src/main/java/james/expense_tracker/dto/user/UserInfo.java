package james.expense_tracker.dto.user;

import james.expense_tracker.model.Role;

public record UserInfo(Long userId, String username, String email, Role role) {}
