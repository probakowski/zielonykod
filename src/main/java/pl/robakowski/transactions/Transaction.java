package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;

@CompiledJson
public record Transaction(String debitAccount, String creditAccount, BigDecimal amount) {
}
