package james.expense_tracker.controller;

import java.util.List;

import james.expense_tracker.model.ExpenseType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import james.expense_tracker.service.ExpenseService;
import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<CreateExpenseResponse> createExpense(
            @RequestBody CreateExpenseRequest request
    ) {
        CreateExpenseResponse response = this.expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseService.ExpenseEntry>> getExpenses() {
        List<ExpenseService.ExpenseEntry> expenseEntries = expenseService.getExpenses();
        return ResponseEntity.status(HttpStatus.OK).body(expenseEntries);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ExpenseService.ExpenseEntry> getExpenseById(
            @PathVariable Long id
    ) {
        try {
            ExpenseService.ExpenseEntry record = this.expenseService.getExpenseById(id);
            return ResponseEntity.status(HttpStatus.OK).body(record);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ExpenseService.ExpenseEntry>> getExpensesByType(
            @PathVariable ExpenseType type
    ) {
        List<ExpenseService.ExpenseEntry> records = this.expenseService.getExpenseByType(type);
        return ResponseEntity.status(HttpStatus.OK).body(records);
    }
}
