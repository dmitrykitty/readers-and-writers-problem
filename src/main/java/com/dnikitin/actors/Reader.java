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
                printAction(getName() + ": Want to enter to the library.");
                library.startReading();

                int timeToRead = 1000 + (int)(Math.random() * 2000);
                printAction(getName() + ": Reading for " + timeToRead + " ms.");


                library.stopReading();
                printAction(getName() + ": Went out.");
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
