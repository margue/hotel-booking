package persistence;

import service.CustomerName;

import java.time.LocalDate;

public class Payment {

    private double paidAmount;
    private double usedAmount;
    private CustomerName customerName;
    private LocalDate paymentDate;

    public Payment(CustomerName customerName, double paidAmount){
        this.customerName = customerName;
        this.paidAmount = paidAmount;
        this.paymentDate = LocalDate.now();
        this.usedAmount= 0.0;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public double getUsedAmount() {
        return usedAmount;
    }

    public CustomerName getCustomerName() {
        return customerName;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void reduceCreditBy(double amount) {
        usedAmount += amount;
    }
}
