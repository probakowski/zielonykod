package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public record Transaction(String debitAccount, String creditAccount,
                          @JsonAttribute(converter = AmountConverter.class) long amount) {
}
