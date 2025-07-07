# Entry 06: The Slotted Page - Organizing Tuples Within a Page

## Objective
To design and implement a SlottedPage format to efficiently manage the storage of variable numbers of tuples within a fixed-size page.

## Key Concepts & Design Decisions

### The Problem with a Simple Layout

Why can't we just pack tuples into a page one after another?

`[Tuple1_Bytes][Tuple2_Bytes][Tuple3_Bytes]...`

This works for `INSERT`, but it's a disaster for `DELETE` and variable-length `UPDATE`.
*   **Deletion:** If we delete `Tuple2`, we leave a "hole" of empty space in the middle of the page. This is called **internal fragmentation**. Over time, our pages would be full of these unusable holes.
*   **Compaction:** We could try to "compact" the page by shifting `Tuple3` over to fill the hole, but this is incredibly slow. We'd have to rewrite a large portion of the page for every single deletion. Even worse, if we have indexes pointing to the physical location of `Tuple3`, all those indexes would now be broken.

### The Solution: The Slotted Page

A Slotted Page elegantly solves these problems by adding a layer of **indirection**. The page is divided into two main sections that grow towards each other:

1.  **The Header:** At the beginning of the page. It contains metadata like:
    *   How many slots are there (`slotCount`).
    *   The starting offset of free space (`freeSpacePointer`).
2.  **The Tuples:** The actual tuple data is stored at the **end** of the page and grows backward towards the header.
3.  **The Slot Array:** In the middle, growing forward after the header. Each slot is a small entry containing:
    *   The offset where the tuple data begins.
    *   The length of the tuple data.

Here's a visual representation of a 4096-byte page:

```
[-----------------------------------------------------------------------------]
^                                                                             ^
Page Start (Byte 0)                                                  Page End (Byte 4095)


[HEADER | SLOT 0 | SLOT 1 | ... | <-- FREE SPACE --> | TUPLE 2 DATA | TUPLE 1 DATA]
 ^        ^                                        ^   ^              ^
 |        |                                        |   |              |
 Byte 0   |                         freeSpacePointer   Tuple 2       Tuple 1 starts here
       slotArray starts here                                         (e.g., at byte 4000)
```

**How it Works:**
*   **Insertion:** To insert a new tuple, we copy its data into the `freeSpacePointer`, add a new slot to the `slotArray`, and update the pointers.
*   **Deletion:** To delete a tuple (say, the one in Slot 1), we don't move any data! We simply mark the slot as empty (e.g., by setting its offset to -1). The tuple's data is now orphaned garbage in the free space, which can be cleaned up later during a "compaction" or "vacuum" process if needed. This is fast and doesn't break any external pointers.
*   **Finding a Tuple:** To find the tuple in Slot `N`, we just read the offset from `slotArray[N]` and go directly to that location.



```
Page Layout:
-------------------------------------------------------------------------------------
| HEADER | SLOT_ARRAY (growing forward) | FREE_SPACE | TUPLE_DATA (growing backward)|
-------------------------------------------------------------------------------------

Header Layout (8 bytes total):
----------------------------------------------------
| slotCount (4 bytes) | freeSpacePointer (4 bytes) |
----------------------------------------------------

Slot Layout (8 bytes total):
-------------------------------------------------
| tupleOffset (4 bytes) | tupleLength (4 bytes) |
-------------------------------------------------
```

## Next Steps
With a robust way to manage tuples within a single page, the final step of the storage layer is to create the HeapFile class. This will act as an abstraction for a table, managing a collection of SlottedPages and providing a high-level API to insert, delete, and scan for tuples across the entire table.