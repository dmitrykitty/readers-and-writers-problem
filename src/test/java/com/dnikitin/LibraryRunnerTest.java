package com.dnikitin;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the LibraryRunner application entry point.
 */
class LibraryRunnerTest {

    /**
     * Verifies that the application starts correctly with no command-line arguments.
     * It should use default values for readers and writers.
     */
    @Test
    void testMainWithNoArgs() throws InterruptedException {
        runMainWithArgs(new String[]{});
    }

    /**
     * Verifies that the application handles two command-line arguments.
     * This covers the branch where numReaders and numWriters are provided.
     */
    @Test
    void testMainWithTwoArgs() throws InterruptedException {
        runMainWithArgs(new String[]{"1", "1"});
    }

    /**
     * Verifies that the application handles three command-line arguments.
     * This covers the full configuration including restingTime.
     */
    @Test
    void testMainWithThreeArgs() throws InterruptedException {
        runMainWithArgs(new String[]{"1", "1", "100"});
    }

    /**
     * Tests the error handling when non-numeric arguments are provided.
     * It verifies that a NumberFormatException is caught and an error message is printed.
     */
    @Test
    void testMainWithInvalidArgs() throws InterruptedException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        runMainWithArgs(new String[]{"invalid", "args"});

        // Verifies the specific error message printed to System.err
        assertTrue(errContent.toString().contains("Invalid arguments. Using defaults."));
        System.setErr(originalErr);
    }

    /**
     * Helper method to run the main method in a separate thread and interrupt it.
     * This is crucial to cover the InterruptedException catch block during join().
     *
     * @param args Command-line arguments for the main method.
     */
    private void runMainWithArgs(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> LibraryRunner.main(args));
        t.start();

        // Allow some time for threads to initialize and enter the join() loop
        Thread.sleep(200);

        // Interrupting the main thread triggers InterruptedException in the join() loop
        t.interrupt();

        // Wait for the thread to terminate to ensure clean test state
        t.join(2000);

        assertFalse(t.isAlive(), "Main thread should terminate after interruption.");
    }
}