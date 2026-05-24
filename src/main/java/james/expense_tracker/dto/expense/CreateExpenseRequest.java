package james.expense_tracker.dto.expense;

public record CreateExpenseRequest(String description, double amount) {
}