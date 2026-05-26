package james.expense_tracker.dto.expense;

import james.expense_tracker.model.ExpenseType;

import java.time.ZonedDateTime;

public record ExpenseEntry(Long id, String description, double amount, ExpenseType type, ZonedDateTime createdAt) {}
