package pl.robakowski.transactions;

import com.dslplatform.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AmountTest {

    private static final Random RANDOM = new Random();

    @Test
    public void amountReaderShouldWork() {
        for (int i = 0; i < 100000; i++) {

            JsonReader<?>reader = new JsonReader<>();
            AmountConverter.JSON_READER.read(reader)
        }
    }

    @Test
    public void checkDouble() {
        for (long i = 92233000000000L; i < 92233720368500L; i++) {
            double v = BigDecimal.valueOf(i, 2).doubleValue();
            assertEquals(i, (long) (v * 100));
        }
    }

    @Test
    public void checkSum() {
        long a = 92233720368500L;
        long b = 0;
        for (long i = 0; i < 100000; i++) {
            b = Math.addExact(b, a);
        }
    }
}