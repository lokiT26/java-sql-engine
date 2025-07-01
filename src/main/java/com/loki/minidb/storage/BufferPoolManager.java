package com.loki.minidb.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BufferPoolManager {

    private final Page[] pagePool;         // The array of pages in memory (our cache)
    private final DiskManager diskManager;
    private final int poolSize;
    private final Map<Integer, Integer> pageTable;  // Maps a pageId from disk to its frameId in the pagePool.
    private final Queue<Integer> freeFrames;    // A queue of frameIds that are free to be used.
    private final int[] pinCount;

    /**
     * Creates a new BufferPoolManager.
     *
     * @param poolSize The number of pages that can be cached in memory at any given time.
     * @param diskManager The disk manager to fetch pages from disk.
     */
    public BufferPoolManager(int poolSize, DiskManager diskManager) {
        this.poolSize = poolSize;
        this.diskManager = diskManager;
        this.pagePool = new Page[poolSize]; // Create the array of Page references

        this.pageTable = new HashMap<>();
        this.freeFrames = new LinkedList<>();

        this.pinCount = new int[poolSize];
        // Loop through the pagePool array and create a new Page object for each slot.
        // This pre-allocates the memory for our cache.
        for (int i = 0; i < poolSize; i++) {
            this.pagePool[i] = new Page();
            // At the beginning, all frames are free.
            // Add the frame ID (which is the index 'i') to the freeFrames queue.
            this.freeFrames.add(i);
            this.pinCount[i] = 0;
        }
    }

    /**
     * Fetches the requested page from the buffer pool.
     * 1. Search the page table for the page.
     * 2. If not found, find a replacement frame from the free list.
     * 3. If no free frame, return null.
     * 4. If a frame is found, update the page table, read page from disk, and return it.
     *
     * @param pageId The ID of the page to fetch.
     * @return The Page object, or null if no free frames are available.
     * @throws IOException if a disk I/O error occurs.
     */
    public Page fetchPage(int pageId) throws IOException {
        Integer frameId;

        // 1. Check if the page is already in the buffer pool (cache hit).
        if (pageTable.containsKey(pageId)) {
            frameId = pageTable.get(pageId);
        } else{
            // 2. If not, it's a cache miss. Find a free frame.
            //    The poll() method retrieves and removes the head of the queue.
            frameId = freeFrames.poll();

            // 3. If no free frames are available, --- logic is remaining ---.
            if (frameId == null) {
                return null;
            }

            // 4. We found a free frame. Now we need to:
            //    a) Update the page table to map the pageId to our new frameId.
            pageTable.put(pageId, frameId);

            //    b) Get the actual Page object (the frame) from our pool.
            //    c) Use the diskManager to read the data from disk into the page's byte array.
            try {
                diskManager.readPage(pageId, this.pagePool[frameId]);
            } catch (IOException e) {
                // Something went wrong reading from disk! We must revert our state.
                pageTable.remove(pageId);
                freeFrames.add(frameId);
                throw e;
            }
        }

        // Increment the pin count for the corresponding frame.
        ++pinCount[frameId];

        return this.pagePool[frameId];
    }
    
    
    /**
     * Unpins a page, allowing it to be evicted if it's not pinned by anyone else.
     *
     * @param pageId The ID of the page to unpin.
     * @return true if the page was successfully unpinned, false if the page was not in memory.
     */
    public boolean unpinPage(int pageId) {
        // 1. Check if the page is in the buffer pool using the pageTable.
        if (!pageTable.containsKey(pageId)) {
            return false;
        }

        // 2. Get the frameId from the pageTable.
        int frameId = pageTable.get(pageId);

        // 3. If the pin count is already 0, something is wrong. Log an error.
        if (pinCount[frameId] <= 0) {
            System.err.println("Error: Unpinning a page with pin count <= 0.");
            return false;
        }

        // 4. Decrement the pin count for this frame.
        --pinCount[frameId];

        // 5. In the future, if the pin count becomes 0, we will notify our LRUReplacer
        //    that this page is now a candidate for eviction.

        return true;
    }
}