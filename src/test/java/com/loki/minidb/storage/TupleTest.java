package com.loki.minidb.storage;

import com.loki.minidb.catalog.Column;
import com.loki.minidb.catalog.Schema;
import com.loki.minidb.catalog.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TupleTest {

    @Test
    void testGetAndSetValues() {
        // 1. ARRANGE: Create a schema for our test tuple.
        // Let's make a schema for (user_id: INTEGER, age: INTEGER)
        Column col1 = new Column("user_id", Type.INTEGER);
        Column col2 = new Column("age", Type.INTEGER);
        Schema schema = new Schema(List.of(col1, col2));

        // Sanity check the schema itself
        assertEquals(8, schema.getTupleLength(), "Tuple length should be 4 (id) + 4 (age) = 8 bytes.");
        assertEquals(0, schema.getColumnOffset(0), "First column offset should be 0.");
        assertEquals(4, schema.getColumnOffset(1), "Second column offset should be 4.");

        // Create a tuple based on this schema.
        Tuple tuple = new Tuple(schema);

        // 2. ACT: Set values for the columns.
        int userId = 101;
        int userAge = 42;
        tuple.setValue(0, userId); // Set user_id
        tuple.setValue(1, userAge);  // Set age

        // 3. ASSERT: Retrieve the values and check if they are correct.
        assertEquals(userId, tuple.getValue(0), "Retrieved user_id should match the value set.");
        assertEquals(userAge, tuple.getValue(1), "Retrieved age should match the value set.");

        // Also, let's check the toString method for a nice representation.
        assertEquals("Tuple{101, 42}", tuple.toString());
    }
    
    @Test
    void testSerializationAndDeserialization() {
        // 1. ARRANGE: Create and populate a tuple as before.
        Column col1 = new Column("user_id", Type.INTEGER);
        Schema schema = new Schema(List.of(col1));
        Tuple originalTuple = new Tuple(schema);
        originalTuple.setValue(0, 999);

        // 2. ACT: "Serialize" the tuple by getting its raw byte data.
        byte[] rawData = originalTuple.getData();
        assertEquals(4, rawData.length);

        // "Deserialize" by creating a new tuple that wraps this raw data.
        Tuple deserializedTuple = new Tuple(rawData, schema);

        // 3. ASSERT: The new tuple should have the same value.
        assertEquals(999, deserializedTuple.getValue(0), "Deserialized tuple should contain the original value.");
    }
}