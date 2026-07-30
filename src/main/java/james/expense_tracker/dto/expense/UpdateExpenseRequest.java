package james.expense_tracker.dto.expense;

import java.math.BigDecimal;

import james.expense_tracker.model.ExpenseType;

public record UpdateExpenseRequest(
    Long id,
    String newDescription,
    BigDecimal newAmount,
    ExpenseType newType) {}