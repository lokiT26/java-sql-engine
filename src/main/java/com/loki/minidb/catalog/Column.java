package com.loki.minidb.catalog;

/**
 * Represents a single column in a table's schema.
 */
public class Column {
    private final String columnName;
    private final Type columnType;

    public Column(String columnName, Type columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public Type getColumnType() {
        return columnType;
    }

    public int getLength() {
        return columnType.getSize();
    }
}