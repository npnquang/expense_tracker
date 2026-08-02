package james.expense_tracker.dto.expense;

import james.expense_tracker.dto.common.CompareOperation;

public record FilterPair(CompareOperation op, String rawValue) {}
