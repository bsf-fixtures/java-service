package fixtures.loans;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoanController {
    private final List<Loan> loans = List.of(
            new Loan("The Dispossessed", "ada", LocalDate.of(2026, 11, 10)),
            new Loan("Middlemarch", "grace", LocalDate.of(2026, 11, 25)),
            new Loan("Invisible Cities", "alan", LocalDate.of(2026, 11, 18)),
            new Loan("A Wizard of Earthsea", "ada", LocalDate.of(2026, 11, 22)));

    public record OverdueLoan(String title, String borrower, long daysLate) {}

    public record BorrowerOverdue(String borrower, int overdueLoans, long totalDaysLate) {}

    /** Loans overdue on the given day (default today), most overdue first. */
    @GetMapping("/loans/overdue")
    public List<OverdueLoan> overdue(@RequestParam(required = false) LocalDate on) {
        LocalDate day = dayOrToday(on);
        return loans.stream()
                .filter(l -> l.overdueOn(day))
                .map(l -> new OverdueLoan(l.title(), l.borrower(), l.daysLate(day)))
                .sorted((a, b) -> Long.compare(b.daysLate(), a.daysLate()))
                .toList();
    }

    /** Borrowers with something overdue on the given day (default today), most days late first. */
    @GetMapping("/borrowers/overdue")
    public List<BorrowerOverdue> overdueByBorrower(@RequestParam(required = false) LocalDate on) {
        LocalDate day = dayOrToday(on);
        return loans.stream()
                .filter(l -> l.overdueOn(day))
                .collect(Collectors.groupingBy(Loan::borrower))
                .entrySet()
                .stream()
                .map(e -> new BorrowerOverdue(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().mapToLong(l -> l.daysLate(day)).sum()))
                .sorted((a, b) -> a.totalDaysLate() == b.totalDaysLate()
                        ? a.borrower().compareTo(b.borrower())
                        : Long.compare(b.totalDaysLate(), a.totalDaysLate()))
                .toList();
    }

    /** The only place the clock is read: the default for a missing {@code ?on=}. */
    private static LocalDate dayOrToday(LocalDate on) {
        return on == null ? LocalDate.now() : on;
    }
}
