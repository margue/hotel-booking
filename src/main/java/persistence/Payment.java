package persistence;

import org.jmolecules.ddd.annotation.ValueObject;
import service.GuestName;

import java.time.LocalDate;

@ValueObject
public class Payment {

    private double paidAmount;
    private double usedAmount;
    private GuestName guestName;
    private LocalDate paymentDate;

    public Payment(GuestName guestName, double paidAmount){
        this.guestName = guestName;
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

    public GuestName getGuestName() {
        return guestName;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void reduceCreditBy(double amount) {
        usedAmount += amount;
    }
}
