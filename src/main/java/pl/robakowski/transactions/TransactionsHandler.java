package pl.robakowski.transactions;

import com.dslplatform.json.JsonWriter;
import pl.robakowski.Handler;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TransactionsHandler extends Handler {

    private static final ThreadLocal<AccountMap> thMap = ThreadLocal.withInitial(AccountMap::new);

    @Override
    protected void handle(InputStream is, JsonWriter writer) throws Exception {
        Iterator<Transaction> transactions = json.iterateOver(Transaction.class, is);
        if (transactions == null) {
            transactions = Collections.emptyIterator();
        }

        AccountMap accountsMap = thMap.get();
        accountsMap.clear();

        while (transactions.hasNext()) {
            Transaction transaction = transactions.next();
            Account creditAccount = accountsMap.get(transaction.creditAccount());
            Account debitAccount = accountsMap.get(transaction.debitAccount());
            long amount = transaction.amount();
            creditAccount.credit(amount);
            debitAccount.debit(amount);
        }

        List<Account> accounts = accountsMap.values();
        json.serialize(writer, accounts);
    }
}
