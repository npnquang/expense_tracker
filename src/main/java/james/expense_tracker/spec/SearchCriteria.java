package james.expense_tracker.spec;

import james.expense_tracker.dto.common.CompareOperation;

public class SearchCriteria<T> {
    private final String key;
    private final CompareOperation operation;
    private final T value;


    public SearchCriteria(String key, CompareOperation operation, T value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public CompareOperation getOperation() {
        return this.operation;
    }

    public T getValue() {
        return this.value;
    }
}
