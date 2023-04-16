package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import org.jetbrains.annotations.NotNull;

@CompiledJson
public class Account implements Comparable<Account> {

    private final String account;
    private int debitCount;
    private int creditCount;
    private Amount balance = new Amount(0, null);

    public Account(String account) {
        this.account = account;
    }

    public Account(String account, int debitCount, int creditCount, Amount balance) {
        this.account = account;
        this.debitCount = debitCount;
        this.creditCount = creditCount;
        this.balance = new Amount(balance);
    }

    public String getAccount() {
        return account;
    }

    public int getDebitCount() {
        return debitCount;
    }

    public int getCreditCount() {
        return creditCount;
    }

    public Amount getBalance() {
        return new Amount(balance);
    }

    public void credit(Amount amount) {
        creditCount++;
        balance.add(amount);
    }

    public void debit(Amount amount) {
        debitCount++;
        balance.sub(amount);
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
