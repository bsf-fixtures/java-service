package fixtures.loans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanTest {
    private final Loan loan = new Loan("t", "b", LocalDate.of(2026, 11, 10));

    @Test
    void isNotOverdueOnItsDueDay() {
        assertFalse(loan.overdueOn(LocalDate.of(2026, 11, 10)));
        assertEquals(0, loan.daysLate(LocalDate.of(2026, 11, 10)));
    }

    @Test
    void countsWholeDaysLate() {
        assertEquals(10, loan.daysLate(LocalDate.of(2026, 11, 20)));
    }
}
