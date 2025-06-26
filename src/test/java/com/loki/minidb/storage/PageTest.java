// The package must match the class it is testing, but in the `test` folder.
package com.loki.minidb.storage;

// Importing the testing tools from the JUnit 5 library.
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains all the unit tests for the Page class.
 */
class PageTest {

    // The `@Test` annotation tells the JUnit framework: "This method is a test case. Run it!"
    @Test
    // The test method name should clearly describe what it is testing.
    void newPageShouldHaveCorrectSize() {
        // We use a pattern called "Arrange-Act-Assert" for clear tests.

        // 1. Arrange: Set up everything you need for the test.
        // In this case, we just need to create a new Page object.
        Page page = new Page();

        // 2. Act: Perform the single action that you want to test.
        // We want to test the data array, so we call the `getData()` method.
        byte[] data = page.getData();

        // 3. Assert: Check if the result of the action is what you expected.
        // `assertNotNull`: This is an assertion. It checks if `data` is not null. If it is null, the test fails.
        assertNotNull(data, "The page's data array should not be null.");

        // `assertEquals`: This assertion checks if two values are equal.
        // It checks if the `length` of our `data` array is equal to `Page.PAGE_SIZE`.
        // If they are not equal, the test fails.
        assertEquals(Page.PAGE_SIZE, data.length, "The page's data should be exactly PAGE_SIZE bytes long.");
    }
}