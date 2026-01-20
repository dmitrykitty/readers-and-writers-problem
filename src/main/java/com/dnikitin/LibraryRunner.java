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
     * Main method to start the simulation.
     * * @param args Command-line arguments:
     * args[0] - Number of readers (default: 10)
     * args[1] - Number of writers (default: 3)
     * args[2] - Resting time in ms (default: 2000)
     */
    public static void main(String[] args) {
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
            } catch (NumberFormatException e) {
                System.err.println("Invalid arguments. Using defaults.");
            }
        }

        Library library = new Library();
        List<Thread> users = new ArrayList<>();

        for (int i = 0; i < numReaders; i++) {
            users.add(new Reader("Reader-" + (i + 1), library, restingTime));
        }
        for (int i = 0; i < numWriters; i++) {
            users.add(new Writer("Writer-" + (i + 1), library, restingTime));
        }

        for (Thread thread : users) {
            thread.start();
        }

        // Keep main alive since others are non-daemon
        try {
            for (Thread thread : users) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
