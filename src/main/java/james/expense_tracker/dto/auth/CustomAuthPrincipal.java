package james.expense_tracker.dto.auth;

public record CustomAuthPrincipal(Long id, String username, String role) {}
