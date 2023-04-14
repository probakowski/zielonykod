package pl.robakowski.transactions;

import com.dslplatform.json.DslJson;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AmountTest {

    @Test
    public void testConvert() throws IOException {

        String s = "{\"amount\"" + IntStream.range(0, 4097).mapToObj(i -> " ").collect(Collectors.joining()) + ": 10.90,\n" +
                "    \"debitAccount\": \"32309111922661937852684864\",\n" +
                "    \"creditAccount\": \"06105023389842834748547303\"\n" +
                "  }";
        InputStream is = new InputStream() {
            int i;

            @Override
            public int read() throws IOException {
                if (i >= s.length()) {
                    return -1;
                }
                return s.charAt(i++);
            }

            @Override
            public int read(byte @NotNull [] b, int off, int len) throws IOException {
                if (i >= s.length()) {
                    return -1;
                }
                b[off] = (byte) s.charAt(i++);
                return 1;
            }
        };

        Transaction deserialize = new DslJson<>().deserialize(Transaction.class, is);
        Assertions.assertNotNull(deserialize);
        Assertions.assertEquals("10.90", deserialize.amount().toString());
    }
}