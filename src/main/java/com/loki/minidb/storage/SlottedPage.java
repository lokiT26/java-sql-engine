package com.loki.minidb.storage;

import java.nio.ByteBuffer;

import com.loki.minidb.catalog.Schema;

/**
 * SlottedPage provides a structured API for a Page that stores tuples.
 *
 * Page Layout:
 * -------------------------------------------------------------------------------------
 * | HEADER | SLOT_ARRAY (growing forward) | FREE_SPACE | TUPLE_DATA (growing backward)|
 * -------------------------------------------------------------------------------------
 *
 * Header Layout (8 bytes total):
 * ----------------------------------------------------
 * | slotCount (4 bytes) | freeSpacePointer (4 bytes) |
 * ----------------------------------------------------
 *
 * Slot Layout (8 bytes total):
 * -------------------------------------------------
 * | tupleOffset (4 bytes) | tupleLength (4 bytes) |
 * -------------------------------------------------
 */
public class SlottedPage {

    // --- Header Constants ---
    private static final int HEADER_SIZE = 8;
    private static final int SLOT_COUNT_OFFSET = 0;
    private static final int FREE_SPACE_POINTER_OFFSET = 4;

    // --- Slot Constants ---
    private static final int SLOT_SIZE = 8;
    private static final int TUPLE_OFFSET_OFFSET = 0; // Relative to slot start
    private static final int TUPLE_LENGTH_OFFSET = 4; // Relative to slot start
    
    private final Page page;
    private final ByteBuffer buffer;

    public SlottedPage(Page page) {
        this.page = page;
        // Wrap the entire page's data in a ByteBuffer for easy I/O
        this.buffer = ByteBuffer.wrap(page.getData());
    }

    // --- Header Accessor Methods ---
    
    public int getSlotCount() {
        return buffer.getInt(SLOT_COUNT_OFFSET);
    }

    private void setSlotCount(int slotCount) {
        buffer.putInt(SLOT_COUNT_OFFSET, slotCount);
    }

    public int getFreeSpacePointer() {
        return buffer.getInt(FREE_SPACE_POINTER_OFFSET);
    }

    private void setFreeSpacePointer(int freeSpacePointer) {
        buffer.putInt(FREE_SPACE_POINTER_OFFSET, freeSpacePointer);
    }
    

    /**
     * Initializes a new, empty slotted page.
     * This should be called once when a page is first formatted.
     */
    public void init() {
        setSlotCount(0);
        // Initially, free space starts right after the header and fills the rest of the page.
        setFreeSpacePointer(Page.PAGE_SIZE);
    }
    

    /**
     * Tries to insert a tuple into the page.
     * @param tuple The tuple to insert.
     * @return The slot number where the tuple was inserted, or null if there's not enough space.
     */
    public Integer insertTuple(Tuple tuple) {
        byte[] tupleData = tuple.getData();
        int tupleLength = tupleData.length;

        // 1. Check for sufficient space. We need space for the tuple data AND a new slot.
        int freeSpace = getFreeSpacePointer() - (HEADER_SIZE + getSlotCount() * SLOT_SIZE);
        if (freeSpace < tupleLength + SLOT_SIZE) {
            return null; // Not enough space
        }

        // 2. We have space. Calculate the new tuple's starting offset.
        int newTupleOffset = getFreeSpacePointer() - tupleLength;

        // 3. Update the free space pointer in the header.
        setFreeSpacePointer(newTupleOffset);

        // 4. Get the ID for the new slot and increment the slot count in the header.
        int slotId = getSlotCount();
        setSlotCount(slotId + 1);

        // 5. Write the tuple data into the page's buffer.
        buffer.put(newTupleOffset, tupleData);

        // 6. Update the new slot in the slot array with the tuple's offset and length.
        int slotOffset = HEADER_SIZE + (slotId * SLOT_SIZE);
        buffer.putInt(slotOffset + TUPLE_OFFSET_OFFSET, newTupleOffset);
        buffer.putInt(slotOffset + TUPLE_LENGTH_OFFSET, tupleLength);

        // 7. Return the new slot ID.
        return slotId;
    }


    /**
     * Retrieves a tuple from a specific slot.
     * @param slotId The slot number of the tuple to retrieve.
     * @param schema The schema to use for interpreting the tuple data.
     * @return The Tuple object, or null if the slot is empty or invalid.
     */
    public Tuple getTuple(int slotId, Schema schema) {
        if (slotId >= getSlotCount()) {
            return null; // Invalid slot
        }

        int slotOffset = HEADER_SIZE + (slotId * SLOT_SIZE);
        int tupleOffset = buffer.getInt(slotOffset + TUPLE_OFFSET_OFFSET);
        int tupleLength = buffer.getInt(slotOffset + TUPLE_LENGTH_OFFSET);

        if (tupleLength == -1) {
            return null; // Slot is empty (deleted tuple)
        }

        byte[] tupleData = new byte[tupleLength];
        // Read the tuple data from the main buffer into our new array
        buffer.get(tupleOffset, tupleData);

        return new Tuple(tupleData, schema);
    }

    
    /**
     * Deletes a tuple from a specific slot by marking the slot as empty.
     * Note: This does not reclaim the space used by the tuple data itself.
     * @param slotId The slot number of the tuple to delete.
     * @return true if deletion was successful, false if slotId was invalid.
     */
    public boolean deleteTuple(int slotId) {
        if (slotId >= getSlotCount()) {
            return false; // Invalid slot
        }

        int slotOffset = HEADER_SIZE + (slotId * SLOT_SIZE);
        
        // Mark the slot as empty by setting its length to -1
        buffer.putInt(slotOffset + TUPLE_LENGTH_OFFSET, -1);
        
        return true;
    }
}