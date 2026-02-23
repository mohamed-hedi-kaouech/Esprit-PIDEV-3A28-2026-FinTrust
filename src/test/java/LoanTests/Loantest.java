package LoanTests;

import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoanTests {

    static LoanService loanService;
    static Loan testLoan;
    static int insertedLoanId;

    @BeforeAll
    static void setup() {

        loanService = new LoanService();

        testLoan = new Loan();
        testLoan.setAmount(10000);
        testLoan.setDuration(12);
        testLoan.setInterestRate(5.0);
        testLoan.setRemainingPrincipal(10000);
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setCreationDate(LocalDateTime.now());
    }

    // ========================
    // ADD
    // ========================
    @Test
    @Order(1)
    void testAddLoan() {

        loanService.Add(testLoan);

        List<Loan> loans = loanService.ReadAll();
        Loan lastLoan = loans.get(loans.size() - 1);

        insertedLoanId = lastLoan.getLoanId();

        assertTrue(insertedLoanId > 0);
    }

    // ========================
    // READ ALL
    // ========================
    @Test
    @Order(2)
    void testReadAllLoans() {

        List<Loan> loans = loanService.ReadAll();

        assertNotNull(loans);
        assertFalse(loans.isEmpty());
    }

    // ========================
    // UPDATE
    // ========================
    @Test
    @Order(3)
    void testUpdateLoan() {

        Loan loan = loanService.ReadId(insertedLoanId);

        loan.setDuration(24);

        loanService.Update(loan);

        Loan updatedLoan = loanService.ReadId(insertedLoanId);

        assertEquals(24, updatedLoan.getDuration());
    }

    // ========================
    // DELETE (CLEANUP)
    // ========================
    @AfterAll
    static void cleanup() {

        if (insertedLoanId != 0) {

            loanService.Delete(insertedLoanId);

            Loan deleted = loanService.ReadId(insertedLoanId);

            if (deleted == null) {
                System.out.println("Loan test deleted successfully!");
            } else {
                System.out.println("Loan test deletion failed!");
            }
        }
    }
}