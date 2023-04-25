package pl.robakowski.transactions;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

@JsonConverter(target = AccountNumber.class)
public class AccountNumberConverter {

    private final static byte[][] DIGITS = new byte[1000][];

    static {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    DIGITS[i * 100 + j * 10 + k] = new byte[]{(byte) (i + '0'), (byte) (j + '0'), (byte) (k + '0')};
                }
            }
        }
    }

    public static final JsonReader.ReadObject<AccountNumber> JSON_READER = reader -> {
        if (reader.last() != '"') {
            throw reader.newParseError("Expecting '\"' for string start");
        }
        // split account number into two 13-digits limbs
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
        // subtracting 53333333333328L has the same cumulative effect as subtracting '0' (i.e. converting from ASCII
        // to digits) in each loop iteration above but uses single operation instead of 13
        return new AccountNumber(l1 - 53333333333328L, l2 - 53333333333328L);
    };

    public static final JsonWriter.WriteObject<AccountNumber> JSON_WRITER = (writer, value) -> {
        if (value == null) {
            writer.writeNull();
            return;
        }
        writer.writeByte((byte) '"');
        serialize(value.l1(), writer);
        serialize(value.l2(), writer);
        writer.writeByte((byte) '"');
    };

    /**
     * Serializes at-most-13-digits number as 13-char string (with leading 0s if needed)
     */
    private static void serialize(long l, JsonWriter writer) {
        writer.writeByte((byte) (l / 1000000000000L + '0')); // first, most significant decimal digit
        writer.writeAscii(DIGITS[(int) ((l / 1000000000) % 1000)]); // digits 2-4
        writer.writeAscii(DIGITS[(int) ((l / 1000000) % 1000)]); // digits 5-7
        writer.writeAscii(DIGITS[(int) ((l / 1000) % 1000)]); // digits 8-10
        writer.writeAscii(DIGITS[(int) (l % 1000)]); // digits 11-13
    }
}
