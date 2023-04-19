package pl.robakowski.transactions;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;

public class AmountConverter {

    public static final JsonReader.ReadObject<Long> JSON_READER = reader -> {
        double v = NumberConverter.deserializeDouble(reader);
        if (v > 922337203685d) {
            String description = String.format("amount %.2f is too big, should be less than or equal 922337203685", v);
            throw reader.newParseError(description);
        }
        return (long) (v * 100);
    };

    public static final JsonWriter.WriteObject<Long> JSON_WRITER = (writer, value) -> {
        if (value == null) {
            writer.writeNull();
            return;
        }
        long l = value / 100;
        String s = Long.toString(l);
        if (value < 0 && l == 0) {
            writer.writeByte((byte) '-');
        }
        int length = s.length();
        for (int i = 0; i < length; i++) {
            writer.writeByte((byte) s.charAt(i));
        }
        writer.writeByte((byte) '.');
        long a = Math.abs(value % 100);
        if (a < 10) {
            writer.writeAscii(new byte[]{'0', (byte) (a + '0')});
        } else {
            writer.writeAscii(new byte[]{(byte) ((a / 10) + '0'), (byte) (a % 10 + '0')});
        }
    };
}
