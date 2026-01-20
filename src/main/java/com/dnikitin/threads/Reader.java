package com.dnikitin.threads;

import com.dnikitin.model.Library;

/**
 * Represents a Reader thread that repeatedly attempts to read from the library.
 */
public class Reader extends Thread {
    private final int restingTime;
    private final Library library;

    public Reader(String name, Library library, int restingTime) {
        super(name);
        this.restingTime = restingTime;
        this.library = library;
    }

    /**
     * Core execution loop. Cycles between reading and resting.
     */
    @Override
    public void run() {
        while (true) {
            try {
                int readingTime = 1000 + (int) (Math.random() * 2001);
                library.startReading(readingTime);

                Thread.sleep(readingTime);

                library.stopReading();
                Thread.sleep(restingTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
