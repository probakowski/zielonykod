package pl.robakowski.transactions;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TransactionsHandler extends Handler {

    public static final Comparator<Account> COMPARATOR = Comparator.comparing(Account::getAccount);

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        Iterator<Transaction> transactions = json.iterateOver(Transaction.class, is);
        if (transactions == null) {
            transactions = Collections.emptyIterator();
        }

        HashMap<AccountNumber, Account> accountsMap = new HashMap<>(135000);
        while (transactions.hasNext()) {
            Transaction transaction = transactions.next();
            Account creditAccount = accountsMap.computeIfAbsent(transaction.creditAccount(), Account::new);
            Account debitAccount = accountsMap.computeIfAbsent(transaction.debitAccount(), Account::new);
            long amount = transaction.amount();
            creditAccount.credit(amount);
            debitAccount.debit(amount);
        }

        List<Account> accounts = new ArrayList<>(accountsMap.values());
        accounts.sort(COMPARATOR);
        json.serialize(writer, accounts);
    }
}
