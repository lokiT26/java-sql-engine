package com.loki.minidb.storage;

import com.loki.minidb.catalog.Schema;
import com.loki.minidb.catalog.Type;

import java.nio.ByteBuffer;

/**
 * Tuple represents a single row in a table.
 */
public class Tuple {

    private final byte[] data;
    private final Schema schema;

    /**
     * Creates a new tuple that conforms to the given schema.
     * Initializes its data array to the correct size.
     * @param schema The schema for this tuple.
     */
    public Tuple(Schema schema) {
        this.schema = schema;
        this.data = new byte[schema.getTupleLength()];
    }

    /**
     * Creates a tuple by wrapping an existing byte array.
     * Used when deserializing a tuple from a page.
     * @param data The byte array containing tuple data.
     * @param schema The schema for this tuple.
     */
    public Tuple(byte[] data, Schema schema) {
        this.data = data;
        this.schema = schema;
    }

    /**
     * Gets the value of a specific column from the tuple.
     * @param columnIndex The index of the column.
     * @return The value of the column as an Object.
     */
    public Object getValue(int columnIndex) {
        // 1. Get the schema for the requested column.
        Type columnType = schema.getColumns().get(columnIndex).getColumnType();
        
        // 2. Get the starting offset for this column in our byte array.
        int offset = schema.getColumnOffset(columnIndex);
        
        // 3. Wrap our data array in a ByteBuffer to make reading typed data easy.
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // 4. Use a switch statement on the column's type to read the correct data type.
        return switch (columnType) {
            case INTEGER -> buffer.getInt(offset);
            // In the future:
            // case VARCHAR -> ... logic to read a string ...
        };
    }

    /**
     * Sets the value of a specific column in the tuple.
     * @param columnIndex The index of the column.
     * @param value The new value for the column.
     */
    public void setValue(int columnIndex, Object value) {
        Type columnType = schema.getColumns().get(columnIndex).getColumnType();
        int offset = schema.getColumnOffset(columnIndex);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        switch (columnType) {
            case INTEGER -> buffer.putInt(offset, (Integer) value);
            // In the future:
            // case VARCHAR -> ...
        }
    }
    
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Tuple{");
        for (int i = 0; i < schema.getColumnCount(); i++) {
            sb.append(getValue(i));
            if (i < schema.getColumnCount() - 1) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}