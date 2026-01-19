package com.dnikitin.actors;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class Library {
    private final CopyOnWriteArrayList<Thread> waitingList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Thread> runningList = new CopyOnWriteArrayList<>();

    private final Semaphore semaphore;

    public Library(int maxReaders) {
        semaphore = new Semaphore(maxReaders, true);
    }

    public void startReading() throws InterruptedException {

        Thread thread = Thread.currentThread();

        synchronized (this) {
            waitingList.add(thread);
            printState(thread.getName() + " joining the queue.");
        }


        semaphore.acquire(1);

        synchronized (this) {
            waitingList.remove(thread);
            runningList.add(thread);

            printState(thread.getName() + " going inside the library.");
        }
    }

    public void stopReading() throws InterruptedException {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            printState(thread.getName() + " going outside the library.");
            runningList.remove(Thread.currentThread());
        }
        semaphore.release(1);
    }

    public void startWriting() throws InterruptedException {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            waitingList.add(thread);
            printState(thread.getName() + " joining the queue.");
        }

        semaphore.acquire(5);

        synchronized (this) {
            waitingList.remove(thread);
            runningList.add(thread);

            printState(thread.getName() + " going inside the library.");
        }
    }

    public void stopWriting() {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            printState(thread.getName() + " going outside the library.");
            runningList.remove(Thread.currentThread());
        }
        semaphore.release(5);
    }

    private synchronized void printState(String message) {
        System.out.println("==================================================");
        System.out.println("Event: " + message);
        System.out.println("==================================================");

        String outside = waitingList.stream()
                .map(Thread::toString)
                .collect(Collectors.joining(","));

        String inside = waitingList.stream()
                .map(Thread::toString)
                .collect(Collectors.joining(","));

        System.out.println("Currently waiting " + waitingList.size() + ": " + outside);
        System.out.println("Currently inside " + runningList.size() + ": " + inside);
        System.out.println();
    }
}
