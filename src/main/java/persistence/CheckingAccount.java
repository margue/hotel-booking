package persistence;

import java.util.ArrayList;
import java.util.List;

// Entity
public class CheckingAccount {

    private final GuestName guestName;

    private final List<Transaction> transactions;

    public CheckingAccount(GuestName guestName) {
        this.guestName = guestName;
        this.transactions = new ArrayList<>();
    }

    public void addPayment(Transaction payment) {
        transactions.add(payment);
    }

    public GuestName guestName() {
        return guestName;
    }

    public Amount credit() {
        return transactions.stream()
                .map(Transaction::amount)
                .reduce(Amount.ZERO, Amount::add);
    }

    public void reduceCreditBy(Amount amount) {
        transactions.add(new Transaction(amount.negate()));
    }
}
