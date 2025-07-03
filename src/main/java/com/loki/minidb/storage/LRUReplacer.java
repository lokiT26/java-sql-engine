package com.loki.minidb.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * LRUReplacer implements the Least Recently Used replacement policy.
 * It tracks pages that are candidates for eviction and provides a victim
 * when the buffer pool is full.
 */
public class LRUReplacer {

    /**
     * A private inner class representing a node in the doubly linked list.
     */
    private static class Node {
        int pageId;
        Node prev;
        Node next;

        Node(int pageId) {
            this.pageId = pageId;
        }
    }

    private final Map<Integer, Node> nodeMap;
    private final Node head;
    private final Node tail;
    private int capacity; // The maximum number of pages the replacer can hold.
    private int size;     // The current number of pages in the replacer.

    /**
     * Creates a new LRUReplacer.
     * @param capacity The maximum number of pages the replacer can track.
     */
    public LRUReplacer(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.nodeMap = new HashMap<>();

        // Initialize sentinel head and tail nodes.
        // This trick avoids null checks when manipulating the list.
        this.head = new Node(-1); // Sentinel node, pageId is irrelevant
        this.tail = new Node(-1); // Sentinel node, pageId is irrelevant

        this.head.next = this.tail;
        this.tail.prev = this.head;
    }
    
    
    // Two private helper methods:
    // 1. removeNode(): Removes a node from the list.
    // 2. moveToFront(): Moves a node to the front of the list (MRU position).


    /**
     * Removes a given node from the doubly linked list.
     * (This is a private helper method).
     * @param node The node to remove.
     */
    private void removeNode(Node node) {
        // Stitch the previous node and next node together, bypassing the current node.
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * Moves a given node to the front of the list (the MRU position).
     * The front is defined as the node right after the head sentinel.
     * (This is a private helper method).
     * @param node The node to move to the front.
     */
    private void moveToFront(Node node) {
        // 1. Set the node's pointers to insert it between head and head.next
        node.next = this.head.next;
        node.prev = this.head;

        // 2. Update the old first node's prev pointer to point to our new node.
        this.head.next.prev = node;

        // 3. Update the head's next pointer to point to our new node.
        this.head.next = node;
    }


    // Three main public methods here:
    // 1. victim():   Finds and returns the LRU page ID.
    // 2. pin():      Called when a page is fetched. Marks it as "used".
    // 3. unpin():    Called when a page's pin count becomes 0. Makes it a victim candidate.


    /**
     * Evicts the least recently used page from the replacer.
     * @return The page ID of the evicted page, or null if the replacer is empty.
     */
    public Integer victim() {
        if (size == 0) {
            return null;
        }
        // 1. Get the LRU node, which is the one just before the tail sentinel.
        Node lruNode = tail.prev;

        // 2. Remove it from the list and the map.
        removeNode(lruNode);
        nodeMap.remove(lruNode.pageId);

        // 3. Decrement the size and return the page ID.
        size--;
        return lruNode.pageId;
    }

    /**
     * This method is called when a page is pinned in the BufferPoolManager.
     * A pinned page should not be in the replacer. If it is, remove it.
     * @param pageId The ID of the page to pin.
     */
    public void pin(int pageId) {
        Node node = nodeMap.get(pageId);
        // If the node exists in our replacer, it means it was a candidate for
        // eviction. But now it's being used, so we must remove it.
        if (node != null) {
            removeNode(node);
            nodeMap.remove(pageId);
            size--;
        }
    }

    /**
     * This method is called when a page's pin count becomes zero in the
     * BufferPoolManager, making it a candidate for eviction.
     * @param pageId The ID of the page to unpin.
     */
    public void unpin(int pageId) {
        // If the page is already in the replacer, it means it's already an
        // eviction candidate. We should move it to the MRU position because
        // it was just "touched" by being unpinned.
        if (nodeMap.containsKey(pageId)) {
            Node node = nodeMap.get(pageId);
            removeNode(node);
            moveToFront(node);
            return;
        }

        // If the replacer is at full capacity, we need to make space.
        if (size >= capacity) {
            victim(); // Evict the LRU page to make room.
        }

        // Add the new page as a candidate to the MRU position.
        Node newNode = new Node(pageId);
        nodeMap.put(pageId, newNode);
        moveToFront(newNode);
        size++;
    }
}