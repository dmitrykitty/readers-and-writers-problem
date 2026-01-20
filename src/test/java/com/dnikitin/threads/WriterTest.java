package com.dnikitin.threads;

import com.dnikitin.model.Library;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class WriterTest {
    @Test
    void testWriterFullCycleAndInterrupt() throws InterruptedException {
        Library mockLibrary = mock(Library.class);
        Writer writer = new Writer("Writer-1", mockLibrary, 10);

        writer.start();
        //wait max for 5 sec until stopWriting will be executed
        verify(mockLibrary, timeout(5000)).stopWriting();

        writer.interrupt();
        writer.join(1000);
        assertFalse(writer.isAlive());
    }

    @Test
    void testWriterInterruptedDuringStart() throws InterruptedException {
        Library mockLibrary = mock(Library.class);
        doThrow(new InterruptedException()).when(mockLibrary).startWriting(anyInt());

        Writer writer = new Writer("Writer-1", mockLibrary, 10);
        writer.start();
        writer.join(1000);

        assertFalse(writer.isAlive());
    }
}
