package com.dnikitin.threads;

import com.dnikitin.model.Library;

/**
 * Represents a Writer thread that repeatedly attempts to write to the library.
 */
public class Writer extends Thread {
    private final int restingTime;
    private final Library library;
    public Writer(String name, Library library, int restingTime) {
        super(name);
        this.restingTime = restingTime;
        this.library = library;
    }

    /**
     * Core execution loop. Cycles between writing and resting.
     */
    @Override
    public void run() {
        while (true) {
            try {
                int writingTime = 1000 + (int)(Math.random() * 2001);
                library.startWriting(writingTime);


                Thread.sleep(writingTime);

                library.stopWriting();
                Thread.sleep(restingTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
