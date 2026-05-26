package james.expense_tracker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.model.ExpenseType;

@Service
public class ExpenseService {

    private final List<ExpenseEntry> expenses = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    public CreateExpenseResponse createExpense(CreateExpenseRequest request) {
        long id = nextId.getAndIncrement();
        expenses.add(new ExpenseEntry(id, request.description(), request.amount(), request.type()));
        return new CreateExpenseResponse(id);
    }

    public List<ExpenseEntry> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public ExpenseEntry getExpenseById(Long id) {
        return expenses.stream().findFirst().filter(e -> e.id().equals(id)).orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public List<ExpenseEntry> getExpenseByType(ExpenseType type) {
        return expenses.stream().filter(e -> e.type().equals(type)).toList();
    }
    public record ExpenseEntry(Long id, String description, double amount, ExpenseType type) {
    }
}
