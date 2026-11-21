package fixtures.loans;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoanController {
    private final List<Loan> loans = List.of(
            new Loan("The Dispossessed", "ada", LocalDate.of(2026, 11, 10)),
            new Loan("Middlemarch", "grace", LocalDate.of(2026, 11, 25)),
            new Loan("Invisible Cities", "alan", LocalDate.of(2026, 11, 18)));

    public record OverdueLoan(String title, String borrower, long daysLate) {}

    /** Loans overdue on the given day (default today), most overdue first. */
    @GetMapping("/loans/overdue")
    public List<OverdueLoan> overdue(@RequestParam(required = false) LocalDate on) {
        LocalDate day = on == null ? LocalDate.now() : on;
        return loans.stream()
                .filter(l -> l.overdueOn(day))
                .map(l -> new OverdueLoan(l.title(), l.borrower(), l.daysLate(day)))
                .sorted((a, b) -> Long.compare(b.daysLate(), a.daysLate()))
                .toList();
    }
}
