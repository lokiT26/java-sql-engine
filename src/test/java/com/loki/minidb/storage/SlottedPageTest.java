package com.loki.minidb.storage;

import com.loki.minidb.catalog.Column;
import com.loki.minidb.catalog.Schema;
import com.loki.minidb.catalog.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlottedPageTest {

    private SlottedPage slottedPage;
    private Schema schema;

    @BeforeEach
    void setUp() {
        // Create a schema for our tests: (id: INTEGER, value: INTEGER)
        this.schema = new Schema(List.of(
                new Column("id", Type.INTEGER),
                new Column("value", Type.INTEGER)
        ));
        // Create a raw page and wrap it with our SlottedPage API
        Page rawPage = new Page();
        this.slottedPage = new SlottedPage(rawPage);
        // Initialize the page with the slotted page format
        this.slottedPage.init();
    }

    @Test
    void testInit() {
        assertEquals(0, slottedPage.getSlotCount(), "A new page should have 0 slots.");
        assertEquals(Page.PAGE_SIZE, slottedPage.getFreeSpacePointer(), "Free space should start at the end of the page.");
    }

    @Test
    void testInsertAndGetTuple() {
        // 1. ARRANGE: Create a tuple to insert
        Tuple tupleToInsert = new Tuple(schema);
        tupleToInsert.setValue(0, 101);
        tupleToInsert.setValue(1, 202);

        // 2. ACT: Insert the tuple
        Integer slotId = slottedPage.insertTuple(tupleToInsert);
        assertNotNull(slotId, "Insertion should be successful and return a slotId.");
        assertEquals(0, slotId, "The first inserted tuple should be in slot 0.");

        // 3. ASSERT: Check header values
        assertEquals(1, slottedPage.getSlotCount(), "Slot count should be 1 after insertion.");
        // Check that free space pointer has moved left by the size of the tuple
        assertEquals(Page.PAGE_SIZE - schema.getTupleLength(), slottedPage.getFreeSpacePointer());

        // 4. ACT & ASSERT: Retrieve the tuple
        Tuple retrievedTuple = slottedPage.getTuple(slotId, schema);
        assertNotNull(retrievedTuple, "Should be able to retrieve the inserted tuple.");
        assertEquals(101, retrievedTuple.getValue(0));
        assertEquals(202, retrievedTuple.getValue(1));
    }

    @Test
    void testDeleteTuple() {
        // Arrange: Insert two tuples
        Tuple tuple1 = new Tuple(schema);
        tuple1.setValue(0, 1);
        Tuple tuple2 = new Tuple(schema);
        tuple2.setValue(0, 2);

        Integer slot1 = slottedPage.insertTuple(tuple1);
        Integer slot2 = slottedPage.insertTuple(tuple2);

        assertEquals(2, slottedPage.getSlotCount());

        // Act: Delete the first tuple
        assertTrue(slottedPage.deleteTuple(slot1), "Deletion should be successful.");

        // Assert: The slot count remains the same, but the tuple is gone
        assertEquals(2, slottedPage.getSlotCount(), "Slot count should not change on delete.");
        assertNull(slottedPage.getTuple(slot1, schema), "Deleted tuple should not be retrievable.");
        assertNotNull(slottedPage.getTuple(slot2, schema), "Other tuples should still be retrievable.");
    }
    
    @Test
    void testPageIsFull() {
        int tupleLength = schema.getTupleLength(); // 8 bytes
        int slotSize = 8;
        int spacePerTuple = tupleLength + slotSize; // 16 bytes needed per tuple

        // Insert tuples until the page is nearly full
        int numToInsert = (Page.PAGE_SIZE - 8) / spacePerTuple; // 8 for the header
        for (int i = 0; i < numToInsert; i++) {
            Tuple t = new Tuple(schema);
            t.setValue(0, i);
            assertNotNull(slottedPage.insertTuple(t), "Should be able to insert tuple " + i);
        }

        // The page should now be full. Try to insert one more.
        Tuple lastTuple = new Tuple(schema);
        lastTuple.setValue(0, 999);
        assertNull(slottedPage.insertTuple(lastTuple), "Should not be able to insert into a full page.");
    }
}