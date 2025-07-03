package com.loki.minidb.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUReplacerTest {

    private LRUReplacer lruReplacer;

    @BeforeEach
    void setUp() {
        // Create a replacer with a capacity of 5 for testing
        lruReplacer = new LRUReplacer(5);
    }

    @Test
    void testUnpinAndVictim() {
        // Unpin some pages, making them candidates for eviction
        lruReplacer.unpin(1); // MRU -> 1
        lruReplacer.unpin(2); // MRU -> 2, 1
        lruReplacer.unpin(3); // MRU -> 3, 2, 1
        lruReplacer.unpin(4); // MRU -> 4, 3, 2, 1

        // The LRU page should be 1
        assertEquals(Integer.valueOf(1), lruReplacer.victim(), "Victim should be page 1");

        // Now the LRU page should be 2
        assertEquals(Integer.valueOf(2), lruReplacer.victim(), "Victim should be page 2");

        // Now the LRU page should be 3
        assertEquals(Integer.valueOf(3), lruReplacer.victim(), "Victim should be page 3");
    }

    @Test
    void testPin() {
        lruReplacer.unpin(1); // MRU -> 1
        lruReplacer.unpin(2); // MRU -> 2, 1
        lruReplacer.unpin(3); // MRU -> 3, 2, 1

        // Pin page 2. It should be removed from the replacer.
        lruReplacer.pin(2);

        // The LRU element is now 1. Victimizing 1 should work.
        assertEquals(Integer.valueOf(1), lruReplacer.victim(), "Victim should be 1, as 2 was pinned.");

        // The next victim should be 3.
        assertEquals(Integer.valueOf(3), lruReplacer.victim(), "Victim should be 3.");

        // The replacer should now be empty.
        assertNull(lruReplacer.victim(), "Replacer should be empty.");
    }

    @Test
    void testCapacity() {
        // Fill the replacer to its capacity
        lruReplacer.unpin(1);
        lruReplacer.unpin(2);
        lruReplacer.unpin(3);
        lruReplacer.unpin(4);
        lruReplacer.unpin(5); // Replacer: 5, 4, 3, 2, 1

        // Try to unpin one more. This should cause page 1 to be evicted
        // from the replacer to make room for page 6.
        lruReplacer.unpin(6); // Replacer: 6, 5, 4, 3, 2

        // Now, the LRU victim should be 2.
        assertEquals(Integer.valueOf(2), lruReplacer.victim(), "Victim should be 2, as 1 was evicted to make capacity.");
    }
    
    @Test
    void testUnpinningExistingMovesToFront() {
        lruReplacer.unpin(1); // MRU -> 1
        lruReplacer.unpin(2); // MRU -> 2, 1
        lruReplacer.unpin(3); // MRU -> 3, 2, 1
        
        // Unpin 1 again. It should move to the front.
        lruReplacer.unpin(1); // MRU -> 1, 3, 2
        
        // The LRU victim should now be 2.
        assertEquals(Integer.valueOf(2), lruReplacer.victim(), "Victim should be 2, as 1 was moved to front.");
    }
}