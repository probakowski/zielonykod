package pl.robakowski.atms;

/**
 * This class implements a vector of 10048 bits.
 */
public class BitSet {

    private final long[] bits = new long[157];

    /**
     * Sets the bit at the specified index to true.
     *
     * @param i number of bit
     * @return true if bit changed status (i.e. it was false before)
     */
    public boolean set(int i) {
        int index = i >> 6;
        long l = bits[index];
        long l1 = l | (1L << i);
        bits[index] = l1;
        return l1 != l;
    }
}
