# Entry 05: Schema and Tuple

## Objective
To define the metadata (Schema, Column, Type) and data container (Tuple) classes that impose structure on raw byte data.

## Key Concepts & Design Decisions
### [`Type.java`](../../src/main/java/com/loki/minidb/storage/Type.java) Class
The `Type` class represents the data types supported by MiniDB. It includes methods for:
- **`int getSize()`**: Returns the size of the data type in bytes.

### [`Column.java`](../../src/main/java/com/loki/minidb/storage/Column.java) Class
The `Column` class represents a single column in a table, including:
- **`String columnName`**: The name of the column.
- **`Type columnType`**: The data type of the column, represented by the `Type` class.

and have methods for:
- **`String getColumnName()`**: Returns the name of the column.
- **`Type getColumnType()`**: Returns the data type of the column.
- **`int getLength()`**: Returns the size of the column in bytes, which is determined by the data type.

### [`Schema.java`](../../src/main/java/com/loki/minidb/storage/Schema.java) Class
The `Schema` class represents the schema of a table, which is a list of columns.
It includes:
- **`List<Column> columns`**: A list of `Column` objects representing the columns in the table.
- **`int tupleLength`**: The total size of a tuple, calculated as the sum of the lengths of all columns.
- **`int[] columnOffsets`**: An array that stores the byte offsets of each column within a tuple, allowing for efficient access to column data.

It Loop through the list of columns, and for each column it stores store its starting offset in the `columnOffsets` array and updates the `tupleLength` accordingly.

### [`Tuple.java`](../../src/main/java/com/loki/minidb/storage/Tuple.java) Class
The `Tuple` class represents a single row of data in a table, encapsulating the values of each column.
It includes:
- **`Schema schema`**: The schema of the table, which defines the structure of the tuple.
- **`byte[] data`**: A byte array that stores the actual data of the tuple, with each column's data stored at its respective offset.

It have methods for:
- **`Object getValue(int columnIndex)`**: Returns the value of the specified column as an `Object`. It retrieves the byte data from the `data` array at the offset defined in `columnOffsets` and converts it to the appropriate type based on the column's data type.
- **`void setValue(int columnIndex, Object value)`**: Sets the value of the specified column. It converts the value to bytes based on the column's data type and stores it in the `data` array at the appropriate offset.
- **`byte[] getData()`**: Returns the raw byte data of the tuple.
- **`String toString()`**: Returns a string representation of the tuple, showing the values of each column.

**`Tuple.java`** uses **`ByteBuffer`** for efficient byte manipulation, allowing it to read and write values of different types (e.g., integers, strings) directly from the byte array.

## Next Steps
The next logical step is to design a page layout (SlottedPage) that can efficiently store a variable number of these tuples within a single 4KB page.