package james.expense_tracker.dto.expense;

import java.time.ZonedDateTime;

import james.expense_tracker.model.ExpenseType;

public record ExpenseEntry(
        Long id, String description, double amount, ExpenseType type, ZonedDateTime createdAt) {}
