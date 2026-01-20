package com.dnikitin.threads;

import com.dnikitin.model.Library;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a Writer thread that repeatedly attempts to write to the library.
 */
public class Writer implements Runnable {
    private final int restingTime;
    private final Library library;
    public Writer(Library library, int restingTime) {
        this.restingTime = restingTime;
        this.library = library;
    }

    /**
     * Core execution loop. Cycles between writing and resting.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int writingTime = ThreadLocalRandom.current().nextInt(1000, 3001);
                library.startWriting(writingTime);


                Thread.sleep(writingTime);

                library.stopWriting();
                Thread.sleep(restingTime);

            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
