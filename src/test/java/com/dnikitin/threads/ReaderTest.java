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
        Reader reader = new Reader("Reader-1", mockLibrary, 10);

        reader.start();

        //wait max for 5 sec until stopReading will be executed
        verify(mockLibrary, timeout(5000)).stopReading();

        reader.interrupt();
        reader.join(1000);

        assertFalse(reader.isAlive());
    }

    @Test
    void testReaderInterruptedDuringStart() throws InterruptedException {
        Library mockLibrary = mock(Library.class);
        // throw exception after start reading
        doThrow(new InterruptedException()).when(mockLibrary).startReading(anyInt());

        Reader reader = new Reader("Reader-1", mockLibrary, 10);
        reader.start();
        reader.join(1000);

        assertFalse(reader.isAlive());
    }
}
