package com.dnikitin.threads;

import com.dnikitin.model.Library;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ReaderTest {

    @Test
    void testReaderFullCycleAndInterrupt() throws InterruptedException {
        Library mockLibrary = mock(Library.class);
        Reader reader = new Reader(mockLibrary, 10);
        Thread vThread = Thread.ofVirtual().name("Reader-1").start(reader);

        //wait max for 5 sec until stopReading will be executed
        verify(mockLibrary, timeout(5000)).stopReading();
        Thread.sleep(100);

        vThread.interrupt();
        vThread.join(1000);

        assertFalse(vThread.isAlive());
    }

    @Test
    void testReaderInterruptedDuringStart() throws InterruptedException {
        Library mockLibrary = mock(Library.class);

        // throw exception after start reading
        doThrow(new InterruptedException()).when(mockLibrary).startReading(anyInt());

        Reader reader = new Reader(mockLibrary, 10);
        Thread vThread = Thread.ofVirtual().name("Reader-1").start(reader);

        vThread.join(1000);

        assertFalse(vThread.isAlive());
        verify(mockLibrary, never()).stopReading();
    }
}
