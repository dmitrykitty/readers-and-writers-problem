package com.dnikitin.actors;

public class Writer extends Thread {
    private final int restingTime;
    private final Library library;
    public Writer(String name, Library library, int restingTime) {
        super(name);
        this.restingTime = restingTime;
        this.library = library;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println(getName() + ": Want to enter to the library.");
                library.startReading();

                int timeToRead = 1000 + (int)(Math.random() * 2000);
                System.out.println(getName() + ": Writing " + timeToRead + " ms.");

                System.out.println(getName() + ": Going out.");

                library.stopReading();
                Thread.sleep(restingTime);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void printAction(String action) {
        System.out.println("[ " + action + " ]" );
    }
}
