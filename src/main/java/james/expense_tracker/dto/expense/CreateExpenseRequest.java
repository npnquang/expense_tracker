package james.expense_tracker.dto.expense;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import james.expense_tracker.model.ExpenseType;

public record CreateExpenseRequest(
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Expense amount is required") BigDecimal amount,
        @NotNull(message = "Expense type is required") ExpenseType type) {}
