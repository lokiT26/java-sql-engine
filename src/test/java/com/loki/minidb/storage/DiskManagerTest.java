package com.loki.minidb.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DiskManagerTest {

    private static final String TEST_DB_FILE = "test_database.db";
    private DiskManager diskManager;
    private Path dbFilePath;

    @BeforeEach
    void setUp() throws IOException {
        // This method runs BEFORE each test.
        dbFilePath = Path.of(TEST_DB_FILE);
        // Create a new DiskManager for the test file.
        diskManager = new DiskManager(TEST_DB_FILE);
    }

    @AfterEach
    void tearDown() throws IOException {
        // This method runs AFTER each test.
        diskManager.close();
        // Delete the test file to clean up.
        Files.deleteIfExists(dbFilePath);
    }

    @Test
    void allocateAndWriteAndRead() throws IOException {
        // 1. ARRANGE: Create a page and fill it with some recognizable data.
        Page writePage = new Page();
        byte[] testData = new byte[Page.PAGE_SIZE];
        for (int i = 0; i < Page.PAGE_SIZE; i++) {
            testData[i] = (byte) (i % 256); // Fill with a repeating pattern
        }
        // System.arraycopy is a fast way to copy arrays.
        System.arraycopy(testData, 0, writePage.getData(), 0, Page.PAGE_SIZE);

        // 2. ACT: Allocate a page, then write our test page to it.
        int newPageId = diskManager.allocatePage();
        diskManager.writePage(newPageId, writePage);

        // 3. ACT: Create a new blank page and read the data back from the disk.
        Page readPage = new Page();
        diskManager.readPage(newPageId, readPage);

        // 4. ASSERT: Check that what we read is the same as what we wrote.
        assertEquals(0, newPageId, "First allocated page ID should be 0.");
        // We use assertArrayEquals for byte arrays, not assertEquals.
        assertArrayEquals(testData, readPage.getData(), "Data read from disk should match data written.");
    }
}