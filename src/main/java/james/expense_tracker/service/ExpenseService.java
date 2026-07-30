package james.expense_tracker.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.dto.expense.ExpenseEntry;
import james.expense_tracker.dto.expense.UpdateExpenseRequest;
import james.expense_tracker.model.Expense;
import james.expense_tracker.model.ExpenseType;
import james.expense_tracker.repository.ExpenseRepository;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private static final Set<String> VALID_SORT_FIELDS = Set.of("id", "amount", "createdAt");
    private static final Set<String> VALID_DIRECTIONS = Set.of("asc", "desc");

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public CreateExpenseResponse createExpense(CreateExpenseRequest request) {
        Expense expense = new Expense(request.description(), request.amount(), request.type());
        Expense saved = this.expenseRepository.save(expense);
        Long id = saved.getId();
        return new CreateExpenseResponse(id);
    }

    public Page<ExpenseEntry> getExpenses(int page, int size, String sortBy, String direction) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "Invalid sortBy field: '%s'. Valid fields: %s",
                            sortBy, VALID_SORT_FIELDS));
        }
        if (!VALID_DIRECTIONS.contains(direction.toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Invalid direction: '%s'. Use 'asc' or 'desc'", direction));
        }
        Sort sort =
                direction.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Expense> expenses = this.expenseRepository.findAll(pageable);
        return expenses.map(
                e ->
                        new ExpenseEntry(
                                e.getId(),
                                e.getDescription(),
                                e.getAmount(),
                                e.getType(),
                                e.getCreatedAt()));
    }

    public ExpenseEntry getExpenseById(Long id) {
        Expense expense =
                this.expenseRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
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

    public void deleteExpense(Long id) {
        Expense expense = this.expenseRepository
                .findById(id)
                .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Expense of id %d not found", id)));

        this.expenseRepository.delete(expense);
    }


    private <T> void updateField(T newValue, Consumer<T> setter) {
        if (newValue == null) {
                return;
        }

        setter.accept(newValue);
    }

    @Transactional
    public void updateExpense(UpdateExpenseRequest request) {
        Long expenseId = request.id();
        Expense expense = this.expenseRepository
                .findById(expenseId)
                .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Expense of id %d not found", expenseId)));
        
        updateField(request.newAmount(), expense::setAmount);
        updateField(request.newDescription(), expense::setDescription);
        updateField(request.newType(), expense::setType);
    }
}
