package com.dnikitin.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LibraryTest {
    private Library library;
    private final int MAX_READERS = 5;

    @BeforeEach
    void setUp() {
        library = new Library();
    }

    /*
    Checks if all 5 readers get inside
     */
    @Test
    @Timeout(value = 5)
    void shouldAllowMultipleReadersUpToLimit() throws InterruptedException {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < MAX_READERS; i++) {
                library.startReading(1000);
            }
            library.stopReading();
        });
    }

    @Test
    @Timeout(value = 5)
    void shouldEnsureWriterExclusivity() throws InterruptedException {
        library.startWriting(1000);

        Thread readerThread = new Thread(() -> {
            try {
                library.startReading(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        readerThread.start();
        // during this 1 sec reader is blocked by writer
        Thread.sleep(1000);

        library.stopWriting();
        // free entracefor reader
        readerThread.join(1000);
        assertThat(readerThread.isAlive()).isFalse();
    }

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
        blockedReader.interrupt(); // Przerywamy czekanie
        blockedReader.join();
    }

    @Test
    @Timeout(value = 5)
    void shouldHandleInterruptedExceptionInStartWriting() throws InterruptedException {
        library.startWriting(1000); // Pisarz w środku

        Thread blockedWriter = new Thread(() -> {
            assertThrows(InterruptedException.class, () -> library.startWriting(1000));
        });

        blockedWriter.start();
        Thread.sleep(200);
        blockedWriter.interrupt();
        blockedWriter.join();
    }

    @Test
    @Timeout(value = 1)
    void shouldCorrectlyUpdateListsOnStop() throws InterruptedException {
        assertDoesNotThrow(() -> {
            library.startReading(1000);
            library.stopReading();
            // if writer able to get in - reader leaved the library
            library.startWriting(1000);
            library.stopWriting();
        });
    }
}
