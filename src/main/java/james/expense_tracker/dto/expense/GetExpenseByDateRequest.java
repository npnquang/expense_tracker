package james.expense_tracker.dto.expense;

import java.time.ZonedDateTime;

public record GetExpenseByDateRequest(ZonedDateTime startDate, ZonedDateTime endDate) {}
