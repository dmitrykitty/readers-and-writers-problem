package com.dnikitin.actors;

public class Reader extends Thread {
    private final int restingTime;
    private final Library library;
    public Reader(String name, Library library, int restingTime) {
        super(name);
        this.restingTime = restingTime;
        this.library = library;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int readingTime = 1000 + (int)(Math.random() * 2001);
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
