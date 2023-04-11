package pl.robakowski.transactions;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Amount {

    private static final BigDecimal DIVISOR = new BigDecimal(100);

    private long l;
    private BigDecimal bd;

    public Amount(long l, BigDecimal bd) {
        this.l = l;
        this.bd = bd;
    }

    public void add(Amount that) {
        if (bd == null && that.bd == null) {
            l += that.l;
            return;
        }
        if (bd == null) {
            bd = new BigDecimal(l).divide(DIVISOR, RoundingMode.DOWN);
        }
        bd = bd.add(that.bd != null ? that.bd : new BigDecimal(that.l));
    }

    public void sub(Amount that) {
        if (bd == null && that.bd == null) {
            l -= that.l;
            return;
        }
        if (bd == null) {
            bd = new BigDecimal(l).divide(DIVISOR, RoundingMode.DOWN);
        }
        bd = bd.subtract(that.bd != null ? that.bd : new BigDecimal(that.l).divide(DIVISOR, RoundingMode.DOWN));
    }

    @Override
    public String toString() {
        JsonWriter writer = new JsonWriter();
        Converter.JSON_WRITER.write(writer, this);
        return writer.toString();
    }

    @JsonConverter(target = Amount.class)
    public static abstract class Converter {
        public static JsonReader.ReadObject<Amount> JSON_READER = reader -> {
            int start = reader.getCurrentIndex();
            char[] chars = reader.readNumber();
            int len = reader.getCurrentIndex() - start + 1;
            for (int i = len - 1; i >= 0 && (chars[i] < '0' || chars[i] > '9'); i--) {
                len--;
            }
            boolean negative = false;
            int first = 0;
            if (chars[0] == '-') {
                negative = true;
                first++;
            }
            int dot = len;
            for (int i = first; i < len; i++) {
                if (chars[i] == '.') {
                    dot = i;
                    break;
                } else if (chars[i] < '0' || chars[i] > '9') {
                    throw reader.newParseError("wrong character, expected digit", start + i);
                }
            }
            if (dot > 12 || len - dot > 3) {
                return new Amount(0, new BigDecimal(new String(chars, 0, len)));
            }
            long v = 0;
            for (int i = first; i < dot; i++) {
                v *= 10;
                v += chars[i] - '0';
            }

            v *= 100;

            if (len - dot == 3) {
                v += (chars[dot + 1] - '0') * 10 + chars[dot + 2] - '0';
            } else {
                v += (chars[dot + 1] - '0') * 10;
            }

            return new Amount(negative ? -v : v, null);
        };
        public static JsonWriter.WriteObject<Amount> JSON_WRITER = (writer, value) -> {
            if (value == null) {
                writer.writeNull();
                return;
            }
            if (value.bd == null) {
                String s = Long.toString(value.l / 100);
                int length = s.length();
                for (int i = 0; i < length; i++) {
                    writer.writeByte((byte) s.charAt(i));
                }
                writer.writeByte((byte) '.');
                long a = Math.abs(value.l % 100);
                if (a == 0) {
                    writer.writeAscii(new byte[]{'0', '0'});
                } else if (a < 10) {
                    writer.writeAscii(new byte[]{'0', (byte) (a + '0')});
                } else {
                    writer.writeAscii(new byte[]{(byte) ((a / 10) + '0'), (byte) (a % 10 + '0')});
                }
                return;
            }
            writer.writeAscii(value.bd.toString());
        };
    }
}
