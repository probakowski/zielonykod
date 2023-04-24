package pl.robakowski.transactions;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountConverter {

    public static final JsonReader.ReadObject<Long> JSON_READER = reader -> {
        BigDecimal bd = NumberConverter.deserializeDecimal(reader).setScale(2, RoundingMode.HALF_DOWN);
        try {
            return bd.unscaledValue().longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Transaction amount is not in supported range");
        }
    };

    public static final JsonWriter.WriteObject<Long> JSON_WRITER = (writer, value) -> {
        if (value == null) {
            writer.writeNull();
        } else {
            NumberConverter.serialize(BigDecimal.valueOf(value, 2), writer);
        }
    };
}
