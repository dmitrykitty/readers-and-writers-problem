package com.dnikitin.threads;

import com.dnikitin.model.Library;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class WriterTest {
    @Test
    void testWriterFullCycleAndInterrupt() throws InterruptedException {
        Library mockLibrary = mock(Library.class);
        Writer writer = new Writer(mockLibrary, 10);
        Thread vThread = Thread.ofVirtual().name("Writer-1").start(writer);

        //wait max for 5 sec until stopWriting will be executed
        verify(mockLibrary, timeout(5000)).stopWriting();
        Thread.sleep(100);

        vThread.interrupt();
        vThread.join(1000);
        assertFalse(vThread.isAlive());
    }

    @Test
    void testWriterInterruptedDuringStart() throws InterruptedException {
        Library mockLibrary = mock(Library.class);
        doThrow(new InterruptedException()).when(mockLibrary).startWriting(anyInt());

        Writer writer = new Writer(mockLibrary, 10);
        Thread vThread = Thread.ofVirtual().name("Writer-1").start(writer);

        vThread.join(1000);

        assertFalse(vThread.isAlive());
        verify(mockLibrary, never()).stopWriting();
    }
}
