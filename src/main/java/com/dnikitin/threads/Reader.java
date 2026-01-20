package com.dnikitin.threads;

import com.dnikitin.model.Library;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a Reader thread that repeatedly attempts to read from the library.
 */
public class Reader implements Runnable {
    private final int restingTime;
    private final Library library;

    public Reader(Library library, int restingTime) {
        this.restingTime = restingTime;
        this.library = library;
    }

    /**
     * Core execution loop. Cycles between reading and resting.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int readingTime = ThreadLocalRandom.current().nextInt(1000, 3001);
                library.startReading(readingTime);

                Thread.sleep(readingTime);

                library.stopReading();
                Thread.sleep(restingTime);

            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
