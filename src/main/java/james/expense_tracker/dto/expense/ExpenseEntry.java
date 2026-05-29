package james.expense_tracker.dto.expense;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import james.expense_tracker.model.ExpenseType;

public record ExpenseEntry(
        Long id,
        String description,
        BigDecimal amount,
        ExpenseType type,
        OffsetDateTime createdAt) {}
