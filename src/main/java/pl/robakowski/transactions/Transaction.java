package pl.robakowski.transactions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

/**
 * This class represents single transaction in bank's system.
 *
 * @param amount value of this transaction expressed in 1/100ths of base currency.
 */
@CompiledJson
public record Transaction(AccountNumber debitAccount, AccountNumber creditAccount,
                          @JsonAttribute(converter = AmountConverter.class) long amount) {

}
