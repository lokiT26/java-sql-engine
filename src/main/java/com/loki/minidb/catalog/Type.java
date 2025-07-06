package com.loki.minidb.catalog;

/**
 * An enum representing the supported data types in our database.
 */
public enum Type {
    INTEGER; // For now, we only support 4-byte integers.

    /**
     * @return The size of this data type in bytes.
     */
    public int getSize() {
        return switch (this) {
            case INTEGER -> 4;
            // In the future, we would add:
            // case VARCHAR -> ... some logic for variable size ...
        };
    }
}