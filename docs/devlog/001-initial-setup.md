# Entry 01: Project Ignition & The Atomic Unit of Storage

## Objective
To establish the foundational structure for the java-sql-engine project, including the Maven build setup, directory layout, and the core Page class, which represents the smallest unit of data transfer between memory and disk.

## Key Concepts & Design Decisions

### `package` declaration:
- Tells Java which "folder" or "group" this class belongs to, It helps organize code and avoid naming conflicts.

### Why We Start with [`Page.java`](../../src/main/java/com/loki/minidb/storage/Page.java)?
- **The Smallest Unit**: A database doesn't read one byte or one record from the disk at a time. That would be incredibly slow. Instead, it reads and writes data in fixed-size chunks called **pages**. 
- A common size is 4 Kilobytes (4096 bytes). 
- The `Page` class represents this single, fundamental chunk of data.

### `Page.java` Implementation Details

- **`public static final int PAGE_SIZE = 4096;`**: The page size is set to a 4KB constant. This is a deliberate choice. Modern operating systems also manage memory in pages, often of the same size. By aligning our database page size with the OS page size, we can achieve more efficient disk I/O. This is our first direct connection to Operating Systems concepts.

- **`private final byte[] data;`**: The core data is a private, final byte array.
    - `private`: This enforces **encapsulation**. No outside class can mess with the page's raw data directly. They must use approved methods (like `getData()`).
    - `final`: This ensures the `data` variable, once assigned to a new byte array in the constructor, cannot be pointed to another array. This prevents a whole class of potential bugs.

### Why We Write [`PageTest.java`](../../src/test/java/com/loki/minidb/storage/PageTest.java)?
- How do we know our `Page.java` code actually works as intended? We could write a main method and print things out, but that's messy and not repeatable.
- **Unit Testing**: A unit test is a small, automated piece of code whose only job is to check that another small piece of code (a "unit," like our `Page` class) works correctly.
- Writing tests for the `Page` class helps us verify that it can read and write data correctly, which is crucial for the entire database engine's functionality.

### Pattern for Clear Tests *(Arrange-Act-Assert)*:
- We use a pattern called "Arrange-Act-Assert" for clear tests.
1. **Arrange**: Arrange: Set up everything you need for the test, like creating a `Page` object and filling it with data.
    ```java
    Page page = new Page();
    ```
2. **Act**: Act: Perform the single action that you want to test, like We want to test the data array, so we call the `getData()` method.
    ```java
    byte[] data = page.getData();
    ```
3. **Assert**: Check if the result of the action is what you expected.

    - `assertNotNull`: This is an assertion. It checks if `data` is not null. If it is null, the test fails.
    - `assertEquals`: This assertion checks if two values are equal. If they are not equal, the test fails.
    ```java
    assertNotNull(data, "The page's data array should not be null.");
    assertEquals(Page.PAGE_SIZE, data.length, "The page's data should be exactly PAGE_SIZE bytes long.");
    ```

## Next Steps
With the Page class defined, the next logical step is to build a `DiskManager` capable of reading these pages from and writing them to a file on the hard disk.