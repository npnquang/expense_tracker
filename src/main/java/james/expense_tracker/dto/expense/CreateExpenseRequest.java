package james.expense_tracker.dto.expense;

import james.expense_tracker.model.ExpenseType;

public record CreateExpenseRequest(String description, double amount, ExpenseType type) {
}