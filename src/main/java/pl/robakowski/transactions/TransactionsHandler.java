package pl.robakowski.transactions;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.*;

public class TransactionsHandler extends Handler {

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        Iterator<Transaction> transactions = json.iterateOver(Transaction.class, is);
        if (transactions == null) {
            transactions = Collections.emptyIterator();
        }

        HashMap<String, Account> accountsMap = new HashMap<>();
        while (transactions.hasNext()) {
            Transaction transaction = transactions.next();
            Account debitAccount = accountsMap.computeIfAbsent(transaction.creditAccount(), Account::new);
            Account creditAccount = accountsMap.computeIfAbsent(transaction.debitAccount(), Account::new);
            long amount = transaction.amount();
            debitAccount.credit(amount);
            creditAccount.debit(amount);
        }

        List<Account> accounts = new ArrayList<>(accountsMap.values());
        accounts.sort(null);
        json.serialize(writer, accounts);
    }
}
