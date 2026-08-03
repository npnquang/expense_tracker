package james.expense_tracker.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import james.expense_tracker.dto.common.CompareOperation;
import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.dto.expense.ExpenseEntry;
import james.expense_tracker.dto.expense.FilterPair;
import james.expense_tracker.dto.expense.GetExpenseRequest;
import james.expense_tracker.dto.expense.UpdateExpenseRequest;
import james.expense_tracker.model.Expense;
import james.expense_tracker.model.ExpenseType;
import james.expense_tracker.repository.ExpenseRepository;
import james.expense_tracker.spec.expense.ExpenseSpec;
import james.expense_tracker.utils.Utils;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private static final Set<String> VALID_SORT_FIELDS = Set.of("createdAt");
    private static final Set<String> VALID_DIRECTIONS = Set.of("asc", "desc");
    private static final Map<String, Period> LOOKBACK_PERIODS = Map.of(
            "1d", Period.ofDays(1),
            "7d", Period.ofDays(7),
            "30d", Period.ofDays(30),
            "90d", Period.ofDays(90),
            "180d", Period.ofDays(180),
            "365d", Period.ofDays(365));

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public CreateExpenseResponse createExpense(CreateExpenseRequest request, Long userId) {
        Expense expense = new Expense(request.description(), request.amount(), request.type(), userId);
        Expense saved = this.expenseRepository.save(expense);
        Long id = saved.getId();
        return new CreateExpenseResponse(id);
    }

    private ExpenseSpec<?> parseExpenseSpec(String key, String rawValue, CompareOperation operation) {
        try {
            if (key.equals("createdAt")) {
                OffsetDateTime value = OffsetDateTime.parse(rawValue);
                return ExpenseSpec.createExpenseSpec(key, operation, value);
            } else if (key.equals("amount")) {
                BigDecimal value = new BigDecimal(rawValue);
                return ExpenseSpec.createExpenseSpec(key, operation, value);

            } else if (key.equals("type")) {
                ExpenseType value = ExpenseType.valueOf(rawValue);
                return ExpenseSpec.createExpenseSpec(key, operation, value);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid key and value to filter");
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid key and value to filter");
        }
    }

    public Page<ExpenseEntry> getExpenses(GetExpenseRequest request, Long userId) {        
        String sortBy = request.sortBy();
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format(
                    "Invalid sortBy field: '%s'. Valid fields: %s",
                    sortBy, VALID_SORT_FIELDS));
                }
        
                String direction = request.direction();
        if (!VALID_DIRECTIONS.contains(direction.toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Invalid direction: '%s'. Use 'asc' or 'desc'", direction));
        }

        String lookback = request.lookback();
        Period lookbackPeriod = LOOKBACK_PERIODS.get(lookback);
        if (lookbackPeriod == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid lookback period: '%s'. Valid periods: %s", lookback, LOOKBACK_PERIODS.keySet()));
        }

        // create user id filter
        ExpenseSpec<Long> userSpec = ExpenseSpec.createExpenseSpec("userId", CompareOperation.EQUAL, userId);

        // create date spec
        OffsetDateTime dateTime = OffsetDateTime.now().minus(lookbackPeriod);
        ExpenseSpec<OffsetDateTime> dateTimeSpec = ExpenseSpec.createExpenseSpec(sortBy, CompareOperation.GREATER_THAN, dateTime);
        Specification<Expense> spec = userSpec.and(dateTimeSpec);

        Map<String, FilterPair> filters = request.filters();
        // combine the specs
        if (filters != null) {
            for (var entry : filters.entrySet()) {
                FilterPair pair = entry.getValue();
                CompareOperation operation = pair.op();
                String rawValue = pair.rawValue();
                
                ExpenseSpec<?> specification = this.parseExpenseSpec(entry.getKey(), rawValue, operation);
                spec.and(specification);
            }            
        }
        
        int page = request.page();
        int size = request.size();
        Sort sort =
                direction.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Expense> expenses = this.expenseRepository.findAll(spec, pageable);
        return expenses.map(
                e ->
                        new ExpenseEntry(
                                e.getId(),
                                e.getDescription(),
                                e.getAmount(),
                                e.getType(),
                                e.getCreatedAt()));
    }

    public ExpenseEntry getExpenseById(Long expenseId, Long userId) {
        Expense expense =
                this.expenseRepository
                        .findByIdAndUserId(expenseId, userId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                String.format("Expense of id %d not found", expenseId)));
        return new ExpenseEntry(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getType(),
                expense.getCreatedAt());
    }

    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = this.expenseRepository
                .findByIdAndUserId(expenseId, userId)
                .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Expense of id %d not found", expenseId)));

        this.expenseRepository.delete(expense);
    }

    @Transactional
    public void updateExpense(UpdateExpenseRequest request, Long userId) {
        Long expenseId = request.id();
        Expense expense = this.expenseRepository
                .findByIdAndUserId(expenseId, userId)
                .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Expense of id %d not found", expenseId)));
        
        Utils.updateField(request.newAmount(), expense::setAmount);
        Utils.updateField(request.newDescription(), expense::setDescription);
        Utils.updateField(request.newType(), expense::setType);
    }
}
