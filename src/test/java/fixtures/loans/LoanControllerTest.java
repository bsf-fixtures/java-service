package fixtures.loans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanControllerTest {
    @Test
    void listsOverdueLoansMostOverdueFirst() {
        var got = new LoanController().overdue(LocalDate.of(2026, 11, 20));
        assertEquals(2, got.size());
        assertEquals("The Dispossessed", got.get(0).title());
        assertEquals(10, got.get(0).daysLate());
        assertEquals("Invisible Cities", got.get(1).title());
    }

    @Test
    void groupsOverdueLoansByBorrowerMostDaysLateFirst() {
        var got = new LoanController().overdueByBorrower(LocalDate.of(2026, 11, 30));
        assertEquals(3, got.size());
        assertEquals("ada", got.get(0).borrower());
        assertEquals(2, got.get(0).overdueLoans());
        assertEquals(28, got.get(0).totalDaysLate());
        assertEquals("alan", got.get(1).borrower());
        assertEquals(1, got.get(1).overdueLoans());
        assertEquals(12, got.get(1).totalDaysLate());
        assertEquals("grace", got.get(2).borrower());
        assertEquals(1, got.get(2).overdueLoans());
        assertEquals(5, got.get(2).totalDaysLate());
    }

    @Test
    void omitsBorrowersWithNothingOverdue() {
        var got = new LoanController().overdueByBorrower(LocalDate.of(2026, 11, 20));
        assertEquals(2, got.size());
        assertEquals("ada", got.get(0).borrower());
        assertEquals(1, got.get(0).overdueLoans());
        assertEquals(10, got.get(0).totalDaysLate());
        assertEquals("alan", got.get(1).borrower());
        assertEquals(1, got.get(1).overdueLoans());
        assertEquals(2, got.get(1).totalDaysLate());
    }

    @Test
    void listsNoBorrowerOnADueDay() {
        var got = new LoanController().overdueByBorrower(LocalDate.of(2026, 11, 10));
        assertEquals(0, got.size());
    }
}
