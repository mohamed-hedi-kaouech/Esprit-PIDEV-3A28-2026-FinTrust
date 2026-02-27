package org.example;
import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Model.Product.EnumProduct.SubscriptionType;
import org.example.Service.BudgetService.BudgetService;
import org.example.Service.ProductService.ProductService;

import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.LoanService;
import org.example.Service.LoanService.RepaymentService;
import org.example.Service.ProductService.ProductSubscriptionService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
//        LoanService ls = new LoanService();
//        Loan l= new Loan(30000,12,4,0);
//        Loan l2= new Loan(30000,14,5,0);
       /*
        ls.Add(l);
        ls.Add(l2);
        System.out.println(ls.ReadAll());
        */
        /*
        System.out.println(ls.ReadId(1));
        Loan l3= new Loan(3,35000,16,LoanStatus.ACTIVE,3.5,500.5);
        ls.Update(l3);
        System.out.println(ls.ReadAll());
        *
         /*
        ls.Delete(1);
        System.out.println(ls.ReadAll());
          */
//        RepaymentService rs = new RepaymentService();
//
//        Loan loan = ls.ReadId(3);
//
//        if (loan == null) {
//            System.out.println("Loan not found!");
//            return;
//        }
//
//        System.out.println("Loan found: " + loan);
//
//        Repayment r1 = new Repayment(
//                loan.getLoanId(),
//                1,
//                3000,
//                2500,
//                500,
//                RepaymentStatus.UNPAID
//        );
//
//        Categorie c = new Categorie("test",123.0,142.0);
//
//        BudgetService  BS = new BudgetService();
//
//        BS.Add(c);

        ProductSubscription PS = new ProductSubscription(1,29, SubscriptionType.MONTHLY);
        ProductSubscriptionService sps = new ProductSubscriptionService();

        sps.Add(PS);


    }
}