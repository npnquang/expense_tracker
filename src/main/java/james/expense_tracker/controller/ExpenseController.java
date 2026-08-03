package james.expense_tracker.controller;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import james.expense_tracker.dto.auth.CustomAuthPrincipal;
import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import james.expense_tracker.dto.expense.ExpenseEntry;
import james.expense_tracker.dto.expense.GetExpenseRequest;
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
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal CustomAuthPrincipal principal) {
        CreateExpenseResponse response = this.expenseService.createExpense(request, principal.id());
        logger.info("Created expense with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ExpenseEntry>> getExpenses(
            @Valid @RequestBody GetExpenseRequest request,
            @AuthenticationPrincipal CustomAuthPrincipal principal
        ) {
        
        Page<ExpenseEntry> expenseEntries =
                expenseService.getExpenses(request, principal.id());
        logger.info(
                "Retrieved {} expenses (page={}, size={}, sortBy={}, direction={}, filters={}, lookback={})",
                expenseEntries.getNumberOfElements(),
                request.page(),
                request.size(),
                request.sortBy(),
                request.direction(),
                request.filters(),
                request.lookback());
        return ResponseEntity.ok(expenseEntries);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ExpenseEntry> getExpenseById(@PathVariable Long id, @AuthenticationPrincipal CustomAuthPrincipal principal) {
        ExpenseEntry record = this.expenseService.getExpenseById(id, principal.id());
        logger.info("Retrieved expense with id: {}", id);
        return ResponseEntity.ok(record);
    }
    
    @PatchMapping
    public ResponseEntity<Void> updateExpense(@RequestBody UpdateExpenseRequest request, @AuthenticationPrincipal CustomAuthPrincipal principal) {
        String newDescription = request.newDescription();
        BigDecimal newAmount = request.newAmount();
        ExpenseType newType = request.newType();
        Long id = request.id();
        this.expenseService.updateExpense(request, principal.id());
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
