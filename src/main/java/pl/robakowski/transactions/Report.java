package pl.robakowski.transactions;

import java.util.*;

public class Report {
    private final HashMap<String, Account> accountsMap = new HashMap<>();

    public void processTransactions(Iterator<Transaction> transactions) {
        while (transactions.hasNext()) {
            Transaction transaction = transactions.next();
            Account debitAccount = accountsMap.computeIfAbsent(transaction.creditAccount(), Account::new);
            Account creditAccount = accountsMap.computeIfAbsent(transaction.debitAccount(), Account::new);
            float amount = transaction.amount();
            debitAccount.debit(amount);
            creditAccount.credit(amount);
        }
    }

    public List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>(accountsMap.values());
        accounts.sort(null);
        return accounts;
    }
}
