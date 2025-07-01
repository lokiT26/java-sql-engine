package com.loki.minidb.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BufferPoolManagerTest {

    private static final String TEST_DB_FILE = "bp_test.db";
    private DiskManager diskManager;
    private BufferPoolManager bufferPoolManager;
    private Path dbFilePath;

    @BeforeEach
    void setUp() throws IOException {
        dbFilePath = Path.of(TEST_DB_FILE);
        // Ensure the file is deleted before each test for a clean slate
        Files.deleteIfExists(dbFilePath);
        diskManager = new DiskManager(TEST_DB_FILE);
        // Let's use a small pool size for easier testing
        bufferPoolManager = new BufferPoolManager(10, diskManager);
    }

    @AfterEach
    void tearDown() throws IOException {
        // We need a way to close the disk manager safely.
        // Let's assume the BufferPoolManager doesn't close the disk manager itself.
        diskManager.close();
        Files.deleteIfExists(dbFilePath);
    }

    @Test
    void testFetchPage_CacheMissAndHit() throws IOException {
        // --- Stage 1: Allocate a page on disk first ---
        // We need a page to exist on disk before we can fetch it.
        int pageId = diskManager.allocatePage();
        assertEquals(0, pageId);

        // --- Stage 2: Test Cache Miss ---
        // The first fetch should be a cache miss.
        Page page1 = bufferPoolManager.fetchPage(pageId);
        assertNotNull(page1, "First fetch should return a valid page.");

        // --- Stage 3: Test Cache Hit ---
        // Fetching the same page again should be a cache hit.
        // It should return the exact same Page object in memory.
        Page page2 = bufferPoolManager.fetchPage(pageId);
        assertNotNull(page2, "Second fetch should also return a valid page.");
        
        // assertSame checks if two references point to the exact same object.
        assertSame(page1, page2, "Cache hit should return the same Page instance.");

        // --- Stage 4: Test Unpinning ---
        // Unpin the page once. The internal pin count should be 1.
        assertTrue(bufferPoolManager.unpinPage(pageId), "Should successfully unpin.");
        // Unpin the page again. The internal pin count should be 0.
        assertTrue(bufferPoolManager.unpinPage(pageId), "Should successfully unpin again.");

        // Fetching it again should still work and return the same instance,
        // as it should still be in the cache.
        Page page3 = bufferPoolManager.fetchPage(pageId);
        assertSame(page1, page3, "Page should still be cached and return same instance.");
        
        // Clean up the last pin
        bufferPoolManager.unpinPage(pageId);
    }

    @Test
    void testBufferPoolIsFull() throws IOException {
        int poolSize = 10;
        
        // Fetch pages to fill up the entire buffer pool
        for (int i = 0; i < poolSize; i++) {
            int pageId = diskManager.allocatePage();
            Page p = bufferPoolManager.fetchPage(pageId);
            assertNotNull(p, "Should be able to fetch page " + i);
            // Don't unpin them, so they stay locked in the pool
        }

        // Now the buffer pool is full and all pages are pinned.
        // Try to fetch one more page.
        int nextPageId = diskManager.allocatePage();
        Page extraPage = bufferPoolManager.fetchPage(nextPageId);
        
        // Since we have no eviction policy yet, this should fail.
        assertNull(extraPage, "Fetching a page when buffer pool is full and all pages are pinned should return null.");

        // Clean up: unpin all the pages
        for (int i = 0; i < poolSize; i++) {
            bufferPoolManager.unpinPage(i);
        }
    }
}