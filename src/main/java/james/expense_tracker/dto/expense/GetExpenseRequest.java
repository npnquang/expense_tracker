package james.expense_tracker.dto.expense;

import java.util.Map;


public record GetExpenseRequest(
    int page,
    int size,
    String sortBy,
    String direction,
    Map<String, FilterPair> filters,
    String lookback
) {

    public GetExpenseRequest(
        int page,
        int size,
        String sortBy,
        String direction,
        Map<String, FilterPair> filters,
        String lookback
    ) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy == null ? "createdAt" : sortBy;
        this.direction = direction == null ? "asc" : direction;
        this.filters = filters;
        this.lookback = lookback == null ? "1d" : lookback;
    }
}
