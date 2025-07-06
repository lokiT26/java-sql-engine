package com.loki.minidb.catalog;

import java.util.List;

public class Schema {

    private final List<Column> columns;
    private final int tupleLength;
    private final int[] columnOffsets;

    /**
     * Creates a new schema.
     * For now, we only support fixed-length tuples.
     * @param columns The list of columns for this schema.
     */
    public Schema(List<Column> columns) {
        this.columns = columns;

        int currentOffset = 0;
        this.columnOffsets = new int[columns.size()];
        
        // --- YOUR LOGIC HERE ---
        // 1. Loop through the list of columns.
        // 2. For each column, store its starting offset in the columnOffsets array.
        // 3. Update the currentOffset for the next column.
        for (int i = 0; i < columns.size(); i++) {
            this.columnOffsets[i] = currentOffset; // The offset for the i-th column
            currentOffset += columns.get(i).getLength();     // Add the size of the i-th column
        }

        // The final offset is the total length of the tuple.
        this.tupleLength = currentOffset;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public int getTupleLength() {
        return tupleLength;
    }

    public int getColumnOffset(int columnIndex) {
        return columnOffsets[columnIndex];
    }
    
    public int getColumnCount() {
        return columns.size();
    }
}