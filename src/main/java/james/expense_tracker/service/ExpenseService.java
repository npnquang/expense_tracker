package james.expense_tracker.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.dto.expense.ExpenseEntry;
import james.expense_tracker.model.Expense;
import james.expense_tracker.model.ExpenseType;
import james.expense_tracker.repository.ExpenseRepository;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public CreateExpenseResponse createExpense(CreateExpenseRequest request) {
        Expense expense = new Expense(request.description(), request.amount(), request.type());
        Expense saved = this.expenseRepository.save(expense);
        Long id = saved.getId();
        return new CreateExpenseResponse(id);
    }

    public List<ExpenseEntry> getExpenses() {
        List<Expense> expenses = this.expenseRepository.findAll();
        return expenses.stream()
                .map(
                        e ->
                                new ExpenseEntry(
                                        e.getId(),
                                        e.getDescription(),
                                        e.getAmount(),
                                        e.getType(),
                                        e.getCreatedAt()))
                .toList();
    }

    public ExpenseEntry getExpenseById(Long id) {
        Expense expense =
                this.expenseRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new HttpClientErrorException(
                                                HttpStatus.NOT_FOUND,
                                                String.format("Expense of id %d not found", id)));
        return new ExpenseEntry(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getType(),
                expense.getCreatedAt());
    }

    public List<ExpenseEntry> getExpenseByType(ExpenseType type) {
        List<Expense> expenses = this.expenseRepository.findByType(type);
        return expenses.stream()
                .map(
                        e ->
                                new ExpenseEntry(
                                        e.getId(),
                                        e.getDescription(),
                                        e.getAmount(),
                                        e.getType(),
                                        e.getCreatedAt()))
                .toList();
    }

    public List<ExpenseEntry> getExpenseByDate(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Expense> expenses = this.expenseRepository.findByCreatedAtBetween(startDate, endDate);
        return expenses.stream()
                .map(
                        e ->
                                new ExpenseEntry(
                                        e.getId(),
                                        e.getDescription(),
                                        e.getAmount(),
                                        e.getType(),
                                        e.getCreatedAt()))
                .toList();
    }
}
