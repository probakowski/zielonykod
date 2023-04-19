package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public record Transaction(AccountNumber debitAccount, AccountNumber creditAccount,
                          @JsonAttribute(converter = AmountConverter.class) long amount) {
}
