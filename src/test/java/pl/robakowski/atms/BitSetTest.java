package pl.robakowski.atms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitSetTest {

    @Test
    void getAndSet() {
        BitSet set = new BitSet();
        for (int i = 0; i < 10048; i++) {
            assertTrue(set.set(i));
            assertFalse(set.set(i));
            assertFalse(set.set(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> set.set(10049));
    }
}