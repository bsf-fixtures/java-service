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
}
