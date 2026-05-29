package james.expense_tracker.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import james.expense_tracker.model.Expense;
import james.expense_tracker.model.ExpenseType;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByType(ExpenseType type);

    List<Expense> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate);
}
