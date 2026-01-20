package com.dnikitin;

import com.dnikitin.model.Library;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for the Library synchronization logic.
 * These tests verify high-level behaviors such as strict FIFO ordering
 * and interaction between different types of actors (Readers and Writers).
 */
class LibraryIntegrationTest {

    /**
     * Verifies that the Library enforces strict First-In-First-Out (FIFO) ordering
     * regardless of the resource requirements.
     * * Even if a Reader could technically fit into the library alongside another Reader,
     * the Turnstile pattern must ensure they wait if a Writer arrived before them.
     *
     * @throws InterruptedException if any thread is interrupted during the test.
     */
    @Test
    void testStrictFIFOOrder() throws InterruptedException {
        // Initialize the library with a limit of 5 readers
        Library library = new Library();

        // list to track the actual order of entry into the library
        List<String> entryOrder = Collections.synchronizedList(new ArrayList<>());

        // Latches are used to control the exact timing of thread arrival at the "door"
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        // 1. The first Reader enters immediately and occupies one permit
        library.startReading(100);
        entryOrder.add("Reader-1");

        // 2. Create a Writer thread that should queue behind Reader-1
        Thread writerThread = new Thread(() -> {
            try {
                latch1.await(); // Wait for the explicit signal to arrive
                library.startWriting(10);
                entryOrder.add("Writer-1");
                library.stopWriting();
            } catch (InterruptedException ignored) {}
        });

        // 3. Create a second Reader thread that should queue behind the Writer
        Thread readerThread2 = new Thread(() -> {
            try {
                latch2.await(); // Wait for the explicit signal to arrive
                library.startReading(10);
                entryOrder.add("Reader-2");
                library.stopReading();
            } catch (InterruptedException ignored) {}
        });

        writerThread.start();
        readerThread2.start();

        // ENFORCING ARRIVAL ORDER:
        // The Writer signals arrival first
        latch1.countDown();
        Thread.sleep(100);  // Buffer to ensure the Writer reaches the queueSemaphore first

        // Reader-2 signals arrival second
        latch2.countDown();
        Thread.sleep(100);

        // Current State:
        // Reader-1 is inside.
        // Writer-1 is at the front of the queue waiting for exclusivity (needs all permits).
        // Reader-2 is blocked at the Turnstile behind Writer-1.

        // Release Reader-1. According to FIFO, Writer-1 MUST be the next to enter.
        library.stopReading();

        // Wait for asynchronous threads to complete their operations
        writerThread.join(2000);
        readerThread2.join(2000);

        // VERIFICATION:
        // Expected entry order: Reader-1 (initial), then Writer-1, then Reader-2
        assertEquals("Reader-1", entryOrder.get(0), "Reader-1 should be first.");
        assertEquals("Writer-1", entryOrder.get(1), "Writer-1 should follow Reader-1 due to FIFO.");
        assertEquals("Reader-2", entryOrder.get(2), "Reader-2 should be last, having arrived after the Writer.");
    }
}
