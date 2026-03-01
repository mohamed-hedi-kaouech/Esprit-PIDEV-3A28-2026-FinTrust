package LoanTests;

import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.RepaymentService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepaymentServiceFullTest {

    private static RepaymentService service;
    private static int savedRepayId;

    @BeforeAll
    static void setup() {
        service = new RepaymentService();
    }

    // =========================
    // 1️⃣ ADD
    // =========================
    @Test
    @Order(1)
    void testAdd() {

        Repayment r = new Repayment(
                1,              // loanId (must exist)
                99,             // special month to avoid conflict
                1000,
                200,
                150,
                50,
                850,
                RepaymentStatus.UNPAID
        );

        service.Add(r);

        List<Repayment> list = service.getByLoan(1);

        assertFalse(list.isEmpty());

        Repayment last = list.get(list.size() - 1);
        savedRepayId = last.getRepayId();

        assertEquals(200, last.getMonthlyPayment());
    }

    // =========================
    // 2️⃣ READ BY ID
    // =========================
    @Test
    @Order(2)
    void testReadById() {

        Repayment r = service.ReadId(savedRepayId);

        assertNotNull(r);
        assertEquals(99, r.getMonth());
    }

    // =========================
    // 3️⃣ UPDATE
    // =========================
    @Test
    @Order(3)
    void testUpdate() {

        Repayment r = service.ReadId(savedRepayId);

        r.setMonthlyPayment(300);
        service.Update(r);

        Repayment updated = service.ReadId(savedRepayId);

        assertEquals(300, updated.getMonthlyPayment());
    }

    // =========================
    // 4️⃣ MARK AS PAID
    // =========================
    @Test
    @Order(4)
    void testMarkAsPaid() {

        service.markAsPaid(savedRepayId);

        Repayment updated = service.ReadId(savedRepayId);

        assertEquals(RepaymentStatus.PAID, updated.getStatus());
    }

    // =========================
    // 5️⃣ CAN PAY
    // =========================
    @Test
    @Order(5)
    void testCanPayRepayment() {

        boolean canPay = service.canPayRepayment(1, 99);

        assertTrue(canPay);
    }

    // =========================
    // 6️⃣ DELETE
    // =========================
    @Test
    @Order(6)
    void testDelete() {

        service.Delete(savedRepayId);

        Repayment deleted = service.ReadId(savedRepayId);

        assertNull(deleted);
    }
}