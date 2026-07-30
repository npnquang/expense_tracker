package james.expense_tracker.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.dto.expense.ExpenseEntry;
import james.expense_tracker.dto.expense.UpdateExpenseRequest;
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
    public ResponseEntity<Page<ExpenseEntry>> getExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<ExpenseEntry> expenseEntries =
                expenseService.getExpenses(page, size, sortBy, direction);
        logger.info(
                "Retrieved {} expenses (page={}, size={}, sortBy={}, direction={})",
                expenseEntries.getNumberOfElements(),
                page,
                size,
                sortBy,
                direction);
        return ResponseEntity.ok(expenseEntries);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ExpenseEntry> getExpenseById(@PathVariable Long id) {
        ExpenseEntry record = this.expenseService.getExpenseById(id);
        logger.info("Retrieved expense with id: {}", id);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ExpenseEntry>> getExpensesByType(@PathVariable ExpenseType type) {
        List<ExpenseEntry> records = this.expenseService.getExpenseByType(type);
        logger.info("Retrieved {} expenses of type: {}", records.size(), type);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/period")
    public ResponseEntity<List<ExpenseEntry>> getExpenseByDate(
            @RequestParam OffsetDateTime startDate, @RequestParam OffsetDateTime endDate) {
        List<ExpenseEntry> records = this.expenseService.getExpenseByDate(startDate, endDate);
        logger.info(
                "Retrieved {} expenses between dates: {} and {}",
                records.size(),
                startDate,
                endDate);
        return ResponseEntity.ok(records);
    }

    @PatchMapping("/")
    public ResponseEntity<Void> updateExpense(@RequestBody UpdateExpenseRequest request) {
        String newDescription = request.newDescription();
        BigDecimal newAmount = request.newAmount();
        ExpenseType newType = request.newType();
        Long id = request.id();
        this.expenseService.updateExpense(request);
        logger.info(
            "Updated expense with id {} with request: description={}, amount={}, type={}",
            id,
            newDescription,
            newAmount,
            newType
        );
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
