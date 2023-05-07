package pl.robakowski.transactions;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of hash map for {@link Account}, using open addressing with linear probing.
 * Should be reused to avoid allocation. Stores up to 524288 records, but performance will suffer it load factor is too big.
 * <p>
 * It stores keys in array of longs along with pointers to values array, in following layout:
 * keys[3*n] - limb 1 (increased by 1) of account number in slot n, 0 means empty slot
 * keys[3*n + 1] - limb 2 of account number in slot n
 * keys[3*n + 2] - index in valuesArr where Account associated with account number is stored
 * This exploits cache of CPU better.
 * <p>
 * It's up to 2x faster than optimized {@link java.util.HashMap}, see AccountMapBenchmark for timings.
 */
public class AccountMap {

    private static final int COUNT = 524288;

    private static final int MASK = COUNT - 1;
    private static final Comparator<Account> COMPARATOR = Comparator.comparing(Account::getAccount);

    private final long[] keys = new long[3 * COUNT];
    private final Account[] valuesArr = new Account[COUNT];

    private int count;
    private boolean sorted;

    private final List<Account> values = Collections.unmodifiableList(new AbstractList<>() {
        @Override
        public Account get(int index) {
            return valuesArr[index];
        }

        @Override
        public int size() {
            return count;
        }
    });

    /**
     * Hash for longs with good dispersion, borrowed from {@link java.util.SplittableRandom}
     */
    private static long hash(AccountNumber number) {
        long z = number.l1() * 31 + number.l2();
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    public Account get(AccountNumber number) {
        if (sorted) {
            throw new IllegalStateException("can't call get() after call to values()");
        }
        int start = (int) (hash(number) & MASK);
        for (int i = start; i < COUNT + start; i++) {
            int index = i & MASK;
            int kIndex = 3 * index;
            if (keys[kIndex] == 0) {
                //we found empty slot
                //value of 0 means no record, increase first limb of account number to avoid confusion
                keys[kIndex] = number.l1() + 1;
                keys[kIndex + 1] = number.l2();
                keys[kIndex + 2] = count;
                Account account = new Account(number);
                valuesArr[count++] = account;
                return account;
            } else if (keys[kIndex] == number.l1() + 1 && keys[kIndex + 1] == number.l2()) {
                // we found value, return it
                return valuesArr[(int) keys[kIndex + 2]];
            }
        }
        throw new IllegalStateException("can't find free spot for new account");
    }

    /**
     * @return list of values stored in this map sorted by {@link Account#getAccount()}
     */
    public List<Account> values() {
        sorted = true;
        Arrays.sort(valuesArr, 0, count, COMPARATOR);
        return values;
    }

    /**
     * Clears
     */
    public void clear() {
        Arrays.fill(keys, 0);
        count = 0;
        sorted = false;
    }
}
