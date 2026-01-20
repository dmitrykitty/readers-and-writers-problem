package com.dnikitin.model;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class Library {
    private final int MAX_READERS = 5;
    private final CopyOnWriteArrayList<Thread> waitingList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Thread> runningList = new CopyOnWriteArrayList<>();

    // queueSemaphore ensures strict FIFO for entering the library
    private final Semaphore queueSemaphore = new Semaphore(1, true);
    // resourceSemaphore manages access to the library (max maxReaders for readers, all for writer)
    private final Semaphore resourceSemaphore;

    public Library() {
        this.resourceSemaphore = new Semaphore(MAX_READERS, true);
    }

    public void startReading(int readingTime) throws InterruptedException {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            waitingList.add(thread);
            printState(thread.getName() + " wants to enter.");
        }

        // Wait in line
        queueSemaphore.acquire();
        try {
            // Once at the front of the queue, wait for resource availability
            resourceSemaphore.acquire(1);
            synchronized (this) {
                waitingList.remove(thread);
                runningList.add(thread);
                printState(thread.getName() + " is inside. Reading for " +  readingTime + " ms.");
            }
        } finally {
            queueSemaphore.release();
        }


    }

    public void stopReading() {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            runningList.remove(thread);
            printState(thread.getName() + " leaves.");
        }
        resourceSemaphore.release(1);
    }

    public void startWriting(int writingTime) throws InterruptedException {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            waitingList.add(thread);
            printState(thread.getName() + " wants to enter.");
        }

        // Wait in line
        queueSemaphore.acquire();
        try {
            // Once at the front of the queue, wait for all permits to ensure exclusivity
            resourceSemaphore.acquire(MAX_READERS);
            synchronized (this) {
                waitingList.remove(thread);
                runningList.add(thread);
                printState(thread.getName() + " is inside. Reading for " +  writingTime + " ms.");
            }
        } finally {
            queueSemaphore.release();
        }
    }

    public void stopWriting() {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            runningList.remove(thread);
            printState(thread.getName() + " leaves.");
        }
        resourceSemaphore.release(MAX_READERS);
    }

    private synchronized void printState(String message) {
        System.out.println("Event: " + message);

        String outside = waitingList.stream()
                .map(Thread::getName)
                .collect(Collectors.joining(", "));

        String inside = runningList.stream()
                .map(Thread::getName)
                .collect(Collectors.joining(", "));

        System.out.println("In queue (" + waitingList.size() + ") : " + outside);
        System.out.println("Inside (" + runningList.size() + ") : " + inside);
        System.out.println("--------------------------------------------------");
    }
}
