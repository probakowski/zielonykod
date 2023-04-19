package pl.robakowski.atms;

public class BitSet {

    private final long[] bits = new long[157];

    public boolean set(int i) {
        int index = i >> 6;
        long l = bits[index];
        long l1 = l | (1L << i);
        bits[index] = l1;
        return l1 != l;
    }
}
