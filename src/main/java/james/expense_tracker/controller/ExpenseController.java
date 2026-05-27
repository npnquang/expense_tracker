package james.expense_tracker.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.dto.expense.ExpenseEntry;
import james.expense_tracker.dto.expense.GetExpenseByDateRequest;
import james.expense_tracker.model.ExpenseType;
import james.expense_tracker.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<CreateExpenseResponse> createExpense(
            @RequestBody CreateExpenseRequest request) {
        CreateExpenseResponse response = this.expenseService.createExpense(request);
        logger.info("Created expense with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseEntry>> getExpenses() {
        List<ExpenseEntry> expenseEntries = expenseService.getExpenses();
        logger.info("Retrieved {} expenses", expenseEntries.size());
        return ResponseEntity.status(HttpStatus.OK).body(expenseEntries);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ExpenseEntry> getExpenseById(@PathVariable Long id) {
        try {
            ExpenseEntry record = this.expenseService.getExpenseById(id);
            logger.info("Retrieved expense with id: {}", id);
            return ResponseEntity.status(HttpStatus.OK).body(record);
        } catch (RuntimeException e) {
            logger.info("Error retrieving expense with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ExpenseEntry>> getExpensesByType(@PathVariable ExpenseType type) {
        List<ExpenseEntry> records = this.expenseService.getExpenseByType(type);
        logger.info("Retrieved {} expenses of type: {}", records.size(), type);
        return ResponseEntity.status(HttpStatus.OK).body(records);
    }

    @GetMapping("/period")
    public ResponseEntity<List<ExpenseEntry>> getExpenseByDate(
            @RequestBody GetExpenseByDateRequest request) {
        List<ExpenseEntry> records = this.expenseService.getExpenseByDate(request);
        logger.info(
                "Retrieved {} expenses between dates: {} and {}",
                records.size(),
                request.startDate(),
                request.endDate());
        return ResponseEntity.status(HttpStatus.OK).body(records);
    }
}
