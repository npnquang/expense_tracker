package james.expense_tracker.dto.expense;

import java.math.BigDecimal;

import james.expense_tracker.model.ExpenseType;

public record CreateExpenseRequest(String description, BigDecimal amount, ExpenseType type) {}
