package pl.robakowski.transactions;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record AccountNumber(long l1, long l2) implements Comparable<AccountNumber> {

    @Override
    public int compareTo(@NotNull AccountNumber o) {
        return Arrays.compare(new long[]{l1, l2}, new long[]{o.l1, o.l2});
    }

}
