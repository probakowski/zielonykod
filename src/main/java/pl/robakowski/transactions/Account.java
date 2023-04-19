package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import org.jetbrains.annotations.NotNull;

@CompiledJson
public class Account implements Comparable<Account> {

    private final AccountNumber account;
    private int debitCount;
    private int creditCount;
    private long balance;

    public Account(AccountNumber account) {
        this.account = account;
    }

    public Account(AccountNumber account, int debitCount, int creditCount, long balance) {
        this.account = account;
        this.debitCount = debitCount;
        this.creditCount = creditCount;
        this.balance = balance;
    }

    public AccountNumber getAccount() {
        return account;
    }

    public int getDebitCount() {
        return debitCount;
    }

    public int getCreditCount() {
        return creditCount;
    }

    @JsonAttribute(converter = AmountConverter.class)
    public long getBalance() {
        return balance;
    }

    public void credit(long amount) {
        creditCount++;
        balance += amount;
    }

    public void debit(long amount) {
        debitCount++;
        balance += amount;
    }

    @Override
    public int compareTo(@NotNull Account account) {
        return this.account.compareTo(account.account);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account1 = (Account) o;
        return account.equals(account1.account);
    }

    @Override
    public int hashCode() {
        return account.hashCode();
    }
}
