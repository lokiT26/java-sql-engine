package com.loki.minidb.storage;

public class Page {

    // This line declares a constant variable. Let's break it down:
    // `public`:  This means any other class in our project can see and use PAGE_SIZE.
    // `static`:  This means the variable belongs to the `Page` class itself, not to any
    //            single `Page` object. There is only one `PAGE_SIZE` value for the whole program.
    // `final`:   This means the value of `PAGE_SIZE` can never be changed once it's set.
    // `int`:     This is the data type, an integer.
    // `4096`:    The actual value. We chose 4KB because it's a standard size that often aligns
    //            with the operating system's own memory page size, which can be efficient.
    public static final int PAGE_SIZE = 4096;

    private final byte[] data;

    public Page() {
        // `new byte[PAGE_SIZE]`: It allocates a new block of memory
        // on the computer that is exactly 4096 bytes long and makes our `data` variable point to it.
        this.data = new byte[PAGE_SIZE];
    }

    /**
    * This is a "getter" method. Since the `data` variable is private, other classes
    * can't access it directly. This public method provides controlled access to it.
    * @return The raw byte array that holds the page's data.
    */
    public byte[] getData() {
        return this.data;
    }
}