package fixtures.loans;

import java.time.LocalDate;

/** A book on loan. Overdue is decided against a day passed in, never the clock, so it is testable. */
public record Loan(String title, String borrower, LocalDate due) {
    public boolean overdueOn(LocalDate today) {
        return today.isAfter(due);
    }

    /** Whole days late; zero when not late. */
    public long daysLate(LocalDate today) {
        return overdueOn(today) ? today.toEpochDay() - due.toEpochDay() : 0;
    }
}
