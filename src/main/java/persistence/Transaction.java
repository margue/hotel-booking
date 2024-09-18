package persistence;

import java.time.LocalDate;
import java.util.UUID;

// Value Object
public class Transaction {

    private final Amount amount;
    private final UUID transactionId;
    private final TransactionDate transactionDate;

    public Transaction(Amount amount) {
        this.amount = amount;
        this.transactionId = UUID.randomUUID();
        this.transactionDate = new TransactionDate(LocalDate.now());
    }

    public Amount amount(){
        return amount;
    }
}
