package pl.robakowski.transactions;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * This class represents 26-digits account number as two 13-digits limbs.
 * Natural ordering is in lexicographical order.
 */
public record AccountNumber(long l1, long l2) implements Comparable<AccountNumber> {

    @Override
    public int compareTo(@NotNull AccountNumber o) {
        return Arrays.compare(new long[]{l1, l2}, new long[]{o.l1, o.l2});
    }

}
