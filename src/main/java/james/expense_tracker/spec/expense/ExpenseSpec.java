package james.expense_tracker.spec.expense;

import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import james.expense_tracker.dto.common.CompareOperation;
import james.expense_tracker.model.Expense;
import james.expense_tracker.spec.SearchCriteria;

public class ExpenseSpec<T extends Comparable<? super T>> implements Specification<Expense> {
    // T is a class that is comparable to itself or its parent class - comparable friendly type
    private final SearchCriteria<T> criteria;
    private static final Set<String> VALID_FIELDS = Set.of("createdAt", "type", "amount");

    public ExpenseSpec(SearchCriteria<T> criteria) {
        this.criteria = criteria;
    }

    public static <T extends Comparable<? super T>> ExpenseSpec<T> createExpenseSpec(String key, CompareOperation operation, T value) {
        if (!VALID_FIELDS.contains(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fields");
        }
        
        SearchCriteria<T> criteria = new SearchCriteria<>(key, operation, value);
        return new ExpenseSpec<T>(criteria);
    }

    @Override
    public Predicate toPredicate(
        Root<Expense> root,
        CriteriaQuery<?> query,
        CriteriaBuilder builder
    ) {
        if (this.criteria.getOperation().equals(CompareOperation.GREATER_THAN)) {
            return builder.greaterThan(root.get(criteria.getKey()), criteria.getValue());
        }

        else if (this.criteria.getOperation().equals(CompareOperation.LESS_THAN)) {
            return builder.lessThan(root.get(criteria.getKey()), criteria.getValue());
        }

        else if (this.criteria.getOperation().equals(CompareOperation.LESS_THAN_OR_EQUAL)) {
            return builder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue());
        }

        else if (this.criteria.getOperation().equals(CompareOperation.GREATER_THAN_OR_EQUAL)) {
            return builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue());
        }

        else if (this.criteria.getOperation().equals(CompareOperation.EQUAL)) {
            return builder.equal(root.get(criteria.getKey()), criteria.getValue());
        }

        else if (this.criteria.getOperation().equals(CompareOperation.LIKE)) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return builder.like(
                    builder.lower(root.get(criteria.getKey()).as(String.class)),
                    "%" + criteria.getValue().toString().toLowerCase() + "%"
                );
            }
            else {
                return null;
            }
        }

        return null;
    }
}
