package LoanTests;

import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.RepaymentService;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepaymentServiceTest {

    private Connection connection;
    private RepaymentService service;

    @BeforeEach
    void setup() throws Exception {

        connection = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");

        // Create tables
        Statement st = connection.createStatement();
        st.execute("""
            CREATE TABLE loan (
                loanId INT AUTO_INCREMENT PRIMARY KEY,
                remaining_principal DOUBLE,
                status VARCHAR(20)
            );
        """);

        st.execute("""
            CREATE TABLE repayment (
                repayId INT AUTO_INCREMENT PRIMARY KEY,
                loanId INT,
                month INT,
                startingBalance DOUBLE,
                monthlyPayment DOUBLE,
                capitalPart DOUBLE,
                interestPart DOUBLE,
                remainingBalance DOUBLE,
                status VARCHAR(20)
            );
        """);

        // Insert test loan
        st.execute("""
            INSERT INTO loan (remaining_principal, status)
            VALUES (1000, 'ACTIVE');
        """);

        service = new RepaymentService();
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    // ===========================
    // TEST ADD
    // ===========================
    @Test
    void testAddRepayment() {

        Repayment r = new Repayment(
                1, 1, 1000,
                200, 150, 50,
                850, RepaymentStatus.UNPAID
        );

        service.Add(r);

        List<Repayment> list = service.ReadAll();

        assertEquals(1, list.size());
        assertEquals(200, list.get(0).getMonthlyPayment());
    }

    // ===========================
    // TEST MARK AS PAID
    // ===========================
    @Test
    void testMarkAsPaid() {

        Repayment r = new Repayment(
                1, 1, 1000,
                200, 150, 50,
                850, RepaymentStatus.UNPAID
        );

        service.Add(r);

        List<Repayment> list = service.ReadAll();
        int id = list.get(0).getRepayId();

        service.markAsPaid(id);

        Repayment updated = service.ReadId(id);

        assertEquals(RepaymentStatus.PAID, updated.getStatus());
    }

    // ===========================
    // TEST CAN PAY
    // ===========================
    @Test
    void testCanPayRepayment() {

        Repayment r1 = new Repayment(
                1, 1, 1000,
                200, 150, 50,
                850, RepaymentStatus.UNPAID
        );

        Repayment r2 = new Repayment(
                1, 2, 850,
                200, 150, 50,
                700, RepaymentStatus.UNPAID
        );

        service.Add(r1);
        service.Add(r2);

        boolean canPayMonth2 =
                service.canPayRepayment(1, 2);

        assertFalse(canPayMonth2);
    }

}