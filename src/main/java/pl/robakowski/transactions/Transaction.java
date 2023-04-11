package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record Transaction(String debitAccount, String creditAccount, Amount amount) {
}
