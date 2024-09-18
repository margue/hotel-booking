package persistence;

import java.util.HashMap;
import java.util.Map;

public class CheckingAccountRepository {
    private Map<GuestName, CheckingAccount> checkingAccounts = new HashMap<>();


    public CheckingAccount load(GuestName guestName) {
        return checkingAccounts.getOrDefault(guestName, new CheckingAccount(guestName));
    }

    public void save(CheckingAccount account) {
        checkingAccounts.put(account.guestName(), account);
    }
}
