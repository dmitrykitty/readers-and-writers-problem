package com.dnikitin.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {
    private Library library;
    private final int MAX_READERS = 5;

    @BeforeEach
    void setUp() {
        library = new Library();
    }

    /**
     * Verifies that the library allows multiple readers up to the defined limit (MAX_READERS).
     */
    @Test
    @Timeout(value = 5)
    void shouldAllowMultipleReadersUpToLimit() throws InterruptedException {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < MAX_READERS; i++) {
                library.startReading(1000);
            }
            assertThat(library.getRunningList()).hasSize(MAX_READERS);
            assertThat(library.getWaitingList()).isEmpty();

            for (int i = 0; i < MAX_READERS; i++) {
                library.stopReading();
            }
            assertThat(library.getRunningList()).isEmpty();
        });
    }

    /**
     * Ensures that a writer has exclusive access, blocking readers from entering during its session.
     */
    @Test
    @Timeout(value = 5)
    void shouldEnsureWriterExclusivity() throws InterruptedException {
        // Current thread becomes the Writer
        library.startWriting(100);
        assertThat(library.getRunningList()).contains(Thread.currentThread());

        Thread readerThread = new Thread(() -> {
            try {
                library.startReading(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        readerThread.start();
        Thread.sleep(500);

        // Verify that the reader is stuck in the waiting list
        assertThat(library.getWaitingList()).contains(readerThread);
        assertThat(library.getRunningList()).doesNotContain(readerThread);

        library.stopWriting();

        // After writer leaves, reader should move from waiting to running
        readerThread.join(1000);
        assertThat(library.getRunningList()).contains(readerThread);
        assertThat(library.getWaitingList()).isEmpty();
    }

    /**
     * Tests that a reader waiting for entry correctly handles and propagates InterruptedException.
     */
    @Test
    @Timeout(value = 5)
    void shouldHandleInterruptedExceptionInStartReading() throws InterruptedException {
        // library is busy
        for (int i = 0; i < MAX_READERS; i++) {
            library.startReading(1000);
        }

        Thread blockedReader = new Thread(() -> {
            assertThrows(InterruptedException.class, () -> library.startReading(1000));
        });

        blockedReader.start();
        Thread.sleep(200);

        // Assert thread is in waiting list before interruption
        assertThat(library.getWaitingList()).contains(blockedReader);

        blockedReader.interrupt();
        blockedReader.join();
    }

    @Test
    @Timeout(value = 5)
    void shouldHandleInterruptedExceptionInStartWriting() throws InterruptedException {
        library.startWriting(1000); // Writer is currently occupying the resource

        Thread blockedWriter = new Thread(() -> assertThrows(InterruptedException.class, () -> library.startWriting(1000)));

        blockedWriter.start();
        Thread.sleep(200);

        assertThat(library.getWaitingList()).contains(blockedWriter);

        blockedWriter.interrupt();
        blockedWriter.join();

        assertFalse(blockedWriter.isAlive(), "Thread should be terminated after interruption");
    }

    @Test
    @Timeout(value = 2)
    void shouldCorrectlyUpdateListsOnStop() throws InterruptedException {
        // 1. Reader enters
        library.startReading(100);
        assertThat(library.getRunningList()).contains(Thread.currentThread());
        assertThat(library.getWaitingList()).isEmpty();

        // 2. Reader leaves
        library.stopReading();
        assertThat(library.getRunningList()).isEmpty();

        // 3. Writer enters
        library.startWriting(100);
        assertThat(library.getRunningList()).contains(Thread.currentThread());

        // 4. Writer leaves
        library.stopWriting();
        assertThat(library.getRunningList()).isEmpty();
        assertThat(library.getWaitingList()).isEmpty();
    }
}
