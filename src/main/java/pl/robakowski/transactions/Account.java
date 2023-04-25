package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public class Account {

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

    /**
     * @return balance of this account expressed in 1/100ths of base currency.
     */
    @JsonAttribute(converter = AmountConverter.class)
    public long getBalance() {
        return balance;
    }

    public void credit(long amount) {
        creditCount++;
        try {
            balance = Math.addExact(balance, amount);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Balance is bigger than " + Long.MAX_VALUE / 100);
        }
    }

    public void debit(long amount) {
        debitCount++;
        try {
            balance = Math.subtractExact(balance, amount);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Balance is smaller than " + Long.MIN_VALUE / 100);
        }
    }
}
