package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@CompiledJson
public class Account implements Comparable<Account> {

    private final String account;
    private int debitCount;
    private int creditCount;
    private BigDecimal balance = new BigDecimal(0);

    public Account(String account) {
        this.account = account;
    }

    public Account(String account, int debitCount, int creditCount, BigDecimal balance) {
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void credit(BigDecimal amount) {
        creditCount++;
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        debitCount++;
        balance = balance.subtract(amount);
    }

    @Override
    public int compareTo(@NotNull Account account) {
        return this.account.compareTo(account.account);
    }
}
