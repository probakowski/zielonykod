package pl.robakowski.transactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Report {
    private final HashMap<String, Account> accountsMap = new HashMap<>();

    public void processTransactions(Iterator<Transaction> transactions) {
        while (transactions.hasNext()) {
            Transaction transaction = transactions.next();
            Account debitAccount = accountsMap.computeIfAbsent(transaction.creditAccount(), Account::new);
            Account creditAccount = accountsMap.computeIfAbsent(transaction.debitAccount(), Account::new);
            Amount amount = transaction.amount();
            debitAccount.credit(amount);
            creditAccount.debit(amount);
        }
    }

    public List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>(accountsMap.values());
        accounts.sort(null);
        return accounts;
    }
}
