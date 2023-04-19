package pl.robakowski.transactions;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record AccountNumber(long l1, long l2) implements Comparable<AccountNumber> {

    @Override
    public int compareTo(@NotNull AccountNumber o) {
        return Arrays.compare(new long[]{l1, l2}, new long[]{o.l1, o.l2});
    }

    @JsonConverter(target = AccountNumber.class)
    public static class AccountNumberConverter {
        public static final JsonReader.ReadObject<AccountNumber> JSON_READER = reader -> {
            if (reader.last() != '"') {
                throw reader.newParseError("Expecting '\"' for string start");
            }
            long l1 = 0;
            for (int i = 0; i < 13; i++) {
                l1 = l1 * 10 + reader.read();
            }
            long l2 = 0;
            for (int i = 0; i < 13; i++) {
                l2 = l2 * 10 + reader.read();
            }
            if (reader.read() != '"') {
                throw reader.newParseError("Expecting '\"' for string end");
            }
            return new AccountNumber(l1 - 1570548400, l2 - 1570548400);
        };

        public static final JsonWriter.WriteObject<AccountNumber> JSON_WRITER = (writer, value) -> {
            if (value == null) {
                writer.writeNull();
                return;
            }
            writer.writeByte((byte) '"');
            NumberConverter.serialize(value.l1, writer);
            NumberConverter.serialize(value.l2, writer);
            writer.writeByte((byte) '"');
        };
    }
}
