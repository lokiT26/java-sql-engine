# Entry 03: The Buffer Pool Manager - Caching and Pinning

## Objective
To implement the core of the BufferPoolManager (BPM), which acts as a caching layer on top of the DiskManager to minimize slow disk I/O.

## Key Concepts & Design Decisions

### What is [`BufferPoolManager.java`](../../src/main/java/com/loki/minidb/storage/BufferPoolManager.java)?

The BufferPoolManager (BPM) is the most critical performance component in the storage engine. Its primary goal is to minimize slow disk I/O by creating an intelligent caching layer between the rest of the database and the DiskManager.

For this initial implementation, the BPM has two main responsibilities:
- **Caching & Middleman**: When a page is requested via `fetchPage()`, the BPM first checks its internal cache (`pagePool`). If the page is present (a **cache hit**), it's returned instantly from memory. If not (a **cache miss**), the BPM uses the `DiskManager` to retrieve the page, places it into a free frame in the pool, and updates its metadata before returning it. From now on, the BPM is the only component that should talk to the `DiskManager`.
- **Pinning for Correctness**: To prevent data corruption (e.g., evicting a page while another part of the system is using it), the BPM uses a "pinning" mechanism. When a page is fetched, its pin count is incremented. A page cannot be a candidate for eviction until its pin count is zero. The component that calls `fetchPage()` is responsible for calling `unpinPage()` when it is done, which decrements the count.

Future responsibilities for the BPM will include a **Eviction Policy (LRU)** and tracking **Dirty Pages** to ensure changes are written back to disk.


### Core Data Structures & Workflow
- **`Page[] pagePool`**: A fixed-size array of Page objects representing the physical memory frames of the cache. This memory is pre-allocated at startup for predictable performance.
- **`Map<Integer, Integer> pageTable`**: The fast lookup table. It maps a disk pageId to a frameId in the pagePool, answering the question "Is this page in the cache, and where?" in O(1) time.
- **`Queue<Integer> freeFrames`**: A simple queue holding the IDs of available frames. This is a bootstrap mechanism for the initial "warm-up" phase of the buffer pool.
- **`int[] pinCount`**: An array that stores the pin count for each frame, crucial for ensuring correctness.


The workflow is managed by two primary methods:
- **`fetchPage(int pageId)`**: Finds the page (either in the cache or from disk), increments its pin count, and returns it.
- **`unpinPage(int pageId)`**: Finds the page in the cache and decrements its pin count. If the pin count reaches zero, this page becomes a potential candidate for future eviction.

## Next Steps
With the core fetching and pinning logic in place, the BPM can't yet handle a full cache. The next step is to build the LRUReplacer, a dedicated component that will implement the Least Recently Used eviction policy. This will allow the BPM to intelligently swap pages between memory and disk when the pool is full.