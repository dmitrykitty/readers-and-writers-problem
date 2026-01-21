package com.dnikitin;

import com.dnikitin.model.Library;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LibraryIntegrationTest {

    @Test
    void testStrictFIFOOrderWithTimeouts() throws InterruptedException {
        Library library = new Library();
        List<String> entryOrder = Collections.synchronizedList(new ArrayList<>());

        // Latches to ensure threads are started and ready to enter the library
        CountDownLatch writerStarted = new CountDownLatch(1);
        CountDownLatch readerStarted = new CountDownLatch(1);

        // Signals from main to release threads to the library entry protocol
        CountDownLatch releaseWriter = new CountDownLatch(1);
        CountDownLatch releaseReader = new CountDownLatch(1);

        // 1. Reader-1 enters immediately and stays inside
        library.startReading(500);
        entryOrder.add("Reader-1");

        // 2. Setup Writer thread (should wait for Reader-1 to leave)
        Thread writerThread = new Thread(() -> {
            try {
                writerStarted.countDown();
                // Use await with timeout returning boolean as requested
                boolean released = releaseWriter.await(5, TimeUnit.SECONDS);
                assertTrue(released, "Writer was not released by main thread in time");

                library.startWriting(100);
                entryOrder.add("Writer-1");
                library.stopWriting();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer-Thread");

        // 3. Setup second Reader thread (should wait for Writer-1 despite having space)
        Thread readerThread2 = new Thread(() -> {
            try {
                readerStarted.countDown();
                boolean released = releaseReader.await(5, TimeUnit.SECONDS);
                assertTrue(released, "Reader-2 was not released by main thread in time");

                library.startReading(100);
                entryOrder.add("Reader-2");
                library.stopReading();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Reader-2-Thread");

        // Start Writer and ensure it reaches the semaphore first
        writerThread.start();
        assertTrue(writerStarted.await(2, TimeUnit.SECONDS), "Writer thread failed to start");
        releaseWriter.countDown();
        Thread.sleep(200); // Small buffer for the writer to call resourceSemaphore.acquire()

        // Start Reader-2 and ensure it arrives after the Writer
        readerThread2.start();
        assertTrue(readerStarted.await(2, TimeUnit.SECONDS), "Reader-2 thread failed to start");
        releaseReader.countDown();
        Thread.sleep(200); // Small buffer for reader-2 to join the queue

        // VERIFY SYSTEM STATE: Reader-1 is inside, others are waiting
        assertThat(library.getRunningList()).contains(Thread.currentThread());
        assertThat(library.getWaitingList()).containsExactly(writerThread, readerThread2);

        // 4. Reader-1 leaves, triggering the chain reaction
        library.stopReading();

        // Join threads with timeout to ensure the whole system completes
        writerThread.join(3000);
        readerThread2.join(3000);

        // FINAL VERIFICATION: Strict FIFO must be maintained
        assertAll(
                () -> assertEquals("Reader-1", entryOrder.getFirst(), "Reader-1 should be the first inside"),
                () -> assertEquals("Writer-1", entryOrder.get(1), "Writer-1 must follow Reader-1 due to FIFO fair policy"),
                () -> assertEquals("Reader-2", entryOrder.get(2), "Reader-2 must wait for the Writer even if space is available"),
                () -> assertThat(library.getRunningList()).isEmpty(),
                () -> assertThat(library.getWaitingList()).isEmpty()
        );
    }
}