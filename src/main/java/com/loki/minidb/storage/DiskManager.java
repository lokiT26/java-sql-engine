package com.loki.minidb.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DiskManager implements AutoCloseable{
    private static final String FILE_MODE = "rwd";

    private final RandomAccessFile dbFile;
    private int nextPageId;

    /**
     * Constructor for the DiskManager.
     * @param dbFilePath The path to the database file.
     * @throws IOException if there's an error opening the file.
     */
    public DiskManager(String dbFilePath) throws IOException{
        // Create a new RandomAccessFile object.
        // The mode "rwd" means:
        // 'r': Open for reading.
        // 'w': Open for writing.
        // 'd': Request that every update to the file's content be written
        //      synchronously to the underlying storage device. This is crucial
        //      for database durability!
        this.dbFile = new RandomAccessFile(dbFilePath, FILE_MODE);

        long fileSize = this.dbFile.length();
        this.nextPageId = (int) (fileSize / Page.PAGE_SIZE);
    }

    /**
     * Reads a specific page from the database file.
     *
     * @param pageId The ID of the page to read (0-indexed).
     * @param page   The Page object to read the data into. This is an "out" parameter.
     *             We pass it in so the caller can provide the memory buffer, which is efficient.
     * @throws IOException if the pageId is invalid or an I/O error occurs.
     */
    public void readPage(int pageId, Page page) throws IOException {
        // 1. Check if the requested pageId is valid.
        if (pageId >= nextPageId) {
            throw new IllegalArgumentException("Cannot read page " + pageId + ": it does not exist.");
        }

        // 2. Calculate the offset in the file where the page starts.
        long offset = pageId * Page.PAGE_SIZE;

        // 3. Seek (jump) to that offset in the file.
        this.dbFile.seek(offset);

        // 4. Read the data from the file into the Page object's byte array.
        this.dbFile.readFully(page.getData());
    }

    /**
     * Writes a page of data to a specific location in the database file.
     *
     * @param pageId The ID of the page to write (0-indexed).
     * @param page   The Page object containing the data to be written.
     * @throws IOException if the pageId is invalid or an I/O error occurs.
     */
    public void writePage(int pageId, Page page) throws IOException {
        // 1. Check if the requested pageId is valid.
        if (pageId >= nextPageId) {
            throw new IllegalArgumentException("Cannot write to page " + pageId + ": it has not been allocated yet.");
        }

        // 2. Calculate the offset. (Same as before)
        long offset = pageId * Page.PAGE_SIZE;

        // 3. Seek to the correct position. (Same as before)
        this.dbFile.seek(offset);

        // 4. Write the page's data to the file.
        this.dbFile.write(page.getData());
    }

    /**
     * Allocates a new page in the database file.
     * This method extends the file by PAGE_SIZE and returns the new page's ID.
     *
     * @return The ID of the newly allocated page.
     * @throws IOException if an I/O error occurs.
     */
    public int allocatePage() throws IOException {
        // 1. The ID for our new page is simply the current value of nextPageId.
        int newPageId = this.nextPageId;

        // 2. Increment the counter for the *next* time this method is called.
        this.nextPageId++;

        // 3. Calculate the new length of the file after adding one page.
        long newFileLength = this.nextPageId * Page.PAGE_SIZE;

        // 4. Set the length of the file to this new size.
        this.dbFile.setLength(newFileLength);

        // 5. Return the ID of the page just created.
        return newPageId;
    }

    @Override
    public void close() throws IOException {
        // This method is required by the AutoCloseable interface.
        // It ensures the file is properly closed when we're done.
        this.dbFile.close();
    }
}
