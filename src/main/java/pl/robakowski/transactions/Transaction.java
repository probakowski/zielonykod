package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.NonNull;

@CompiledJson
public record Transaction(String debitAccount, String creditAccount, Amount amount) {

    public Transaction(String debitAccount, String creditAccount, Amount amount) {
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
        this.amount = new Amount(amount);
    }

    public Amount amount() {
        return new Amount(amount);
    }
}
