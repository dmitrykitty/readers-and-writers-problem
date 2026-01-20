package com.dnikitin;

import com.dnikitin.model.Library;
import com.dnikitin.threads.Reader;
import com.dnikitin.threads.Writer;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the Readers-Writers simulation.
 * Handles command-line arguments and initializes the environment.
 */
public class LibraryRunner {
    /**
     * Data container for simulation settings.
     */
    public record SimulationParams(int numReaders, int numWriters, int restingTime) {}

    /**
     * Extracts simulation parameters from command-line arguments.
     *
     * @param args Command-line arguments.
     * @return Configuration object with parsed or default values.
     */
    public static SimulationParams parseArguments(String[] args) {
        int numReaders = 10;
        int numWriters = 3;
        int restingTime = 2000;

        if (args.length >= 2) {
            try {
                numReaders = Integer.parseInt(args[0]);
                numWriters = Integer.parseInt(args[1]);
                if (args.length >= 3) {
                    restingTime = Integer.parseInt(args[2]);
                }
            } catch (NumberFormatException _) {
                System.err.println("Invalid arguments. Using defaults.");
            }
        }
        return new SimulationParams(numReaders, numWriters, restingTime);
    }

    /**
     * Main method to start the simulation.
     * @param args Command-line arguments:
     * args[0] - Number of readers (default: 10)
     * args[1] - Number of writers (default: 3)
     * args[2] - Resting time in ms (default: 2000)
     */
    public static void main(String[] args) throws Exception {
        SimulationParams params = parseArguments(args);

        Library library = new Library();
        List<Thread> virtualThreads = new ArrayList<>();

        // Create and start virtual threads manually to keep references for joining
        for (int i = 0; i < params.numReaders(); i++) {
            String name = "Reader-" + (i + 1);
            Thread t = Thread.ofVirtual().name(name).start(new Reader(library, params.restingTime()));
            virtualThreads.add(t);
        }

        for (int i = 0; i < params.numWriters(); i++) {
            String name = "Writer-" + (i + 1);
            Thread t = Thread.ofVirtual().name(name).start(new Writer(library, params.restingTime()));
            virtualThreads.add(t);
        }

        // CRITICAL: Since virtual threads are daemons, we MUST join them
        // to prevent the main thread from exiting immediately.
        try {
            for (Thread t : virtualThreads) {
                t.join();
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}

