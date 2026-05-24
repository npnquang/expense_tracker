package james.expense_tracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import james.expense_tracker.service.ExpenseService;
import james.expense_tracker.dto.expense.CreateExpenseRequest;
import james.expense_tracker.dto.expense.CreateExpenseResponse;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<CreateExpenseResponse> createExpense(
            @RequestBody CreateExpenseRequest request) {
        CreateExpenseResponse response = this.expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ExpenseService.ExpenseEntry> getExpenses() {
        return expenseService.getExpenses();
    }

}
