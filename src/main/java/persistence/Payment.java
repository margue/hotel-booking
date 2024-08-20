package persistence;

import java.time.LocalDate;

public class Payment {

    private final Amount paidAmount;
    private Amount usedAmount;
    private final GuestName guestName;
    private final PaymentDate paymentDate;

    public Payment(GuestName guestName, Amount paidAmount){
        this.guestName = guestName;
        this.paidAmount = paidAmount;
        this.paymentDate = new PaymentDate(LocalDate.now());
        this.usedAmount= new Amount(0.0);
    }

    public static int compareByPaymentDate(Payment p1, Payment p2) {
        return p1.getPaymentDate().paymentDate().isEqual(p2.getPaymentDate().paymentDate()) ? 0 :
                p1.getPaymentDate().paymentDate().isBefore(p2.getPaymentDate().paymentDate()) ? -1 : 1;
    }

    public Amount getRemainingCredit() {
        return getPaidAmount().subtract(getUsedAmount());
    }

    public Amount getPaidAmount() {
        return paidAmount;
    }

    public Amount getUsedAmount() {
        return usedAmount;
    }

    public GuestName getGuestName() {
        return guestName;
    }

    public PaymentDate getPaymentDate() {
        return paymentDate;
    }

    public void reduceCreditBy(Amount amount) {
        usedAmount = usedAmount.add(amount);
    }
}
