package james.expense_tracker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;

@Service
public class ExpenseService {

    private final List<ExpenseEntry> expenses = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    public CreateExpenseResponse createExpense(CreateExpenseRequest request) {
        long id = nextId.getAndIncrement();
        expenses.add(new ExpenseEntry(id, request.description(), request.amount()));
        return new CreateExpenseResponse(id);
    }

    public List<ExpenseEntry> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public record ExpenseEntry(Long id, String description, double amount) {
    }
}
