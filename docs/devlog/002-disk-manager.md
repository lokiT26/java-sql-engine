# Entry 02: The Disk Manager

## Objective
Goal is to implement a `DiskManager` that handles the reading and writing of `Page` objects to a file on disk, and also allocates new pages as needed. 

## Key Concepts & Design Decisions

### What is [`DiskManager.java`](../../src/main/java/com/loki/minidb/storage/DiskManager.java)?
- **DiskManager**: This class is responsible for managing the physical storage of data on disk. It handles reading and writing `Page` objects to a file, ensuring that data is stored in a structured and efficient manner.
- **File Operations**: It uses Java's file I/O capabilities to read from and write to a file, which is crucial for persistent storage in a database system.

### What is `AutoCloseable`?
- **`AutoCloseable`**: This is an interface in Java that allows an object to be used in a **try-with-resources** statement. When the try block is exited, the `close()` method is automatically called, ensuring that resources are released properly.
- **Why Use It?**: By implementing `AutoCloseable`, we ensure that the `DiskManager` can be used in a try-with-resources block, which automatically closes the file resources when done, preventing resource leaks.

### Why Use `RandomAccessFile`?
- **`RandomAccessFile`**: This class allows us to read from and write to a file at any position, which is essential for a database system that needs to access specific pages directly.
- **Why Not Just `FileInputStream` or `FileOutputStream`?**:
  - `FileInputStream` and `FileOutputStream` are sequential access streams, meaning they read or write data in a linear fashion. This is not efficient for a database that needs to jump to specific locations in the file.
  - `RandomAccessFile` allows us to seek to any position in the file, making it ideal for our use case where we need to read and write specific pages.

### Core Methods of [`DiskManager`](../../src/main/java/com/loki/minidb/storage/DiskManager.java)
1. **`readPage(int pageId, Page page)`**: This method reads a specific page from the disk file. It calculates the position in the file based on the page number and reads the corresponding bytes into a `Page` object.
2. **`writePage(int pageId, Page page)`**: This method writes a `Page` object to the disk file. It calculates the position based on the page number and writes the page's data to that position in the file.
3. **`allocatePage()`**: This method allocates a new page in the disk file. It finds the next available page ID and initializes a new `Page` object, which can then be written to the disk. Helps when database is empty or when new pages are needed.
4. **`close()`**: This method closes the `RandomAccessFile`, ensuring that all resources are released properly. It is called automatically when the `DiskManager` is used in a try-with-resources block.

### Testing the DiskManager ([`DiskManagerTest`](../../src/test/java/com/loki/minidb/storage/DiskManagerTest.java))
- **`@BeforeEach`**: This annotation is used to indicate that the method should be run before each test case. It sets up the environment for each test, ensuring that the `DiskManager` starts with a fresh state. (E.g., creating a new `DiskManager` instance and initializing the test file).
- **`@AfterEach`**: This annotation is used to clean up after each test case. It ensures that the test environment is reset, preventing side effects from one test affecting another. (E.g., deleting the test file after each test).
- `allocateAndWriteAndRead()`: This test method demonstrates the complete cycle of allocating a page, writing data to it, and then reading it back to verify correctness. It uses assertions to check that the data read matches the data written, ensuring that the `DiskManager` functions correctly.


## Next Steps
With a DiskManager to handle the low-level disk I/O, we can now build a smarter caching layer on top of it: the `BufferPoolManager`. This will keep frequently used pages in memory to avoid constant, slow disk access.