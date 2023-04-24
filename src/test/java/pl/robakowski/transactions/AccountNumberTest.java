package pl.robakowski.transactions;

import com.dslplatform.json.DslJson;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountNumberTest {


    private static final Random RANDOM = new Random();
    private static final DslJson<Object> json = new DslJson<>();

    @Test
    public void readerShouldWork() throws IOException {
        for (int i = 0; i < 1000000; i++) {
            long l1 = RANDOM.nextLong(10000000000000L);
            long l2 = RANDOM.nextLong(10000000000000L);
            String s = String.format("\"%013d%013d\"", l1, l2);
            assertEquals(28, s.length());
            ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            AccountNumber actual = json.deserialize(AccountNumber.class, is);
            assertEquals(new AccountNumber(l1, l2), actual, s);
        }
    }

    @Test
    public void writerShouldWork() throws IOException {
        for (int i = 0; i < 1000000; i++) {
            long l1 = RANDOM.nextLong(10000000000000L);
            long l2 = RANDOM.nextLong(10000000000000L);
            String s = String.format("\"%013d%013d\"", l1, l2);
            assertEquals(28, s.length());
            AccountNumber accountNumber = new AccountNumber(l1, l2);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            json.serialize(accountNumber, os);
            assertEquals(s, os.toString(StandardCharsets.UTF_8));
        }
    }
}