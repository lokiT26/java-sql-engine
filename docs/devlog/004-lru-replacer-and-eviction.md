# Entry 04: The LRU Replacer and a Fully Evicting Buffer Pool

## Objective
To implement a standalone LRUReplacer component and integrate it into the BufferPoolManager to enable a fully functional, eviction-capable caching system.

## Key Concepts & Design Decisions

### 1. [`LRUReplacer`](../../src/main/java/com/loki/minidb/storage/LRUReplacer.java) Design
The LRUReplacer is a dedicated component responsible for managing page eviction based on the Least Recently Used (LRU) policy. It tracks which pages are currently in the buffer pool and determines which page to evict when a new page needs to be fetched, ensuring that the least recently used page is removed first.

**Data Structures:**
- **`Map<Integer, Node> nodeMap`**: A hash map that maps page IDs to their corresponding nodes in the doubly linked list. This allows O(1) access to nodes for quick updates.
- **`Node head` and `Node tail`**: Sentinel nodes that simplify the management of the doubly linked list, which represents the order of page usage. (Seninel nodes are dummy nodes that do not hold actual page data but help in managing the list structure.)
- **`int size`**: The current number of pages in the LRUReplacer, which helps in determining when to evict a page.
- **`int capacity`**: The maximum number of pages that can be held in the LRUReplacer, which is set to the size of the buffer pool.

**Private Methods:**
- **`void removeNode(Node node)`**: Removes a node from the doubly linked list.
- **`void moveToFront(Node node)`**: Moves a node to the front of the list, marking it as the most recently used.

**Public Methods:**
- **`Integer victim()`**: Returns the page ID of the least recently used page, removes it from the LRUReplacer, and updates the internal structures accordingly.
- **`void pin(int pageId)`**: Marks a page as pinned, removing it from the LRUReplacer if it exists.
- **`void unpin(int pageId)`**: This method is called when a page's pin count becomes zero in the BufferPoolManager, making it a candidate for eviction.

These methods deals with only `pageId`s, enforcing separation of concerns. The LRUReplacer does not directly interact with the BufferPoolManager's frames or pin counts, allowing it to focus solely on eviction logic.

### 2. Integration with [`BufferPoolManager`](../../src/main/java/com/loki/minidb/storage/BufferPoolManager.java)

**Changes in `fetchPage()`:** When buffer pool is full, BufferPoolManager uses the LRUReplacer to determine which page to evict when a new page needs to be fetched and the buffer pool is at capacity. It calls the `victim()` method, that returns the page ID of LRU page.

**Changes in `unpinPage()`:** When a pin count hits zero in BufferPoolManager, `unpin(int pageId)` is called to make the page an eviction candidate.

## Next Steps
With the core storage infrastructure complete—from disk I/O to intelligent caching—the next step is to impose structure on our data. Will implement Tuple and Slotted Page layouts to store records within pages, and a Heap File abstraction to organize pages into tables.