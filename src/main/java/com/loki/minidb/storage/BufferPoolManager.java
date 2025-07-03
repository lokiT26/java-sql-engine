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
    private final LRUReplacer lruReplacer;

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
        this.lruReplacer = new LRUReplacer(poolSize);
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
     * 3. If no free frame, then evict a LRU page, and finds its frame.
     * 4. If a frame is found, update the page table, read page from disk, pin page, and return it.
     * 5. If a frame is not found, then return null.
     *
     * @param pageId The ID of the page to fetch.
     * @return The Page object, or null if no free frames, and no unpinned pages in cache are available.
     * @throws IOException if a disk I/O error occurs.
     */
    public Page fetchPage(int pageId) throws IOException {
        // 1. Check if page is already in the buffer pool (cache hit).
        if (pageTable.containsKey(pageId)) {
            int frameId = pageTable.get(pageId);
            pinCount[frameId]++;
            // A page that is fetched is being used, so it's not a candidate for eviction.
            lruReplacer.pin(pageId);
            return pagePool[frameId];
        }

        // 2. Cache miss. Find a replacement frame.
        // First, try to get a frame from the free list.
        Integer frameId = freeFrames.poll();

        // If the free list is empty, we must evict a page.
        if (frameId == null) {
            // Ask the LRUReplacer for a victim page ID.
            Integer victimPageId = lruReplacer.victim();

            // If victim() returns null, all pages are pinned. We cannot proceed.
            if (victimPageId == null) {
                return null;
            }

            // We have the victim's pageId. Now find its frameId using the pageTable.
            frameId = pageTable.get(victimPageId);

            // Important: Remove the old page's mapping from the page table.
            pageTable.remove(victimPageId);
            
            // Note: Will handle dirty pages here later. If the victim page was
            // modified, we would need to write it to disk before evicting.
        }

        // 3. We now have a valid frameId to use, either from the free list or eviction.
        
        // 4. Update metadata for the new page.
        pageTable.put(pageId, frameId);
        pinCount[frameId] = 1;

        // 5. The new page is being used, so tell the replacer to "pin" it
        lruReplacer.pin(pageId);

        // 6. Read the page data from disk into the frame.
        diskManager.readPage(pageId, pagePool[frameId]);
        
        return pagePool[frameId];
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
        pinCount[frameId]--;

        // 5. If the pin count is now 0, this page becomes a candidate for eviction.
        if (pinCount[frameId] == 0) {
            lruReplacer.unpin(pageId);
        }

        return true;
    }
}