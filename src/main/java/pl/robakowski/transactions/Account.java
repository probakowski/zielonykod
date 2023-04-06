package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import org.jetbrains.annotations.NotNull;

@CompiledJson
public class Account implements Comparable<Account> {

    private final String account;
    private int debitCount;
    private int creditCount;
    private float balance;

    public Account(String account) {
        this.account = account;
    }

    public Account(String account, int debitCount, int creditCount, float balance) {
        this.account = account;
        this.debitCount = debitCount;
        this.creditCount = creditCount;
        this.balance = balance;
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

    public float getBalance() {
        return balance;
    }

    public void credit(float amount) {
        creditCount++;
        balance += amount;
    }

    public void debit(float amount) {
        debitCount++;
        balance -= amount;
    }

    @Override
    public int compareTo(@NotNull Account account) {
        return this.account.compareTo(account.account);
    }
}
