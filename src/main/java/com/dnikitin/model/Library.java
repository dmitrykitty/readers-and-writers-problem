package com.dnikitin.model;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * Monitors access to the library resource.
 * Implements the Turnstile pattern to ensure fair FIFO access
 * and prevents writer starvation.
 */
public class Library {
    private final int MAX_READERS = 5;
    private final CopyOnWriteArrayList<Thread> waitingList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Thread> runningList = new CopyOnWriteArrayList<>();

    // manages access to the library (max maxReaders for readers, all for writer) and FIFO order
    private final Semaphore resourceSemaphore = new Semaphore(MAX_READERS, true);


    /**
     * Requests entry for a reader.
     * Uses a turnstile to maintain arrival order and then acquires a single resource permit.
     *
     * @param readingTime Duration in ms for the simulated reading task.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void startReading(int readingTime) throws InterruptedException {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            waitingList.add(thread);
            printState(thread.getName() + " wants to enter.");
        }

        // Wait in line

        // Once at the front of the queue, wait for resource availability
        resourceSemaphore.acquire(1);
        synchronized (this) {
            waitingList.remove(thread);
            runningList.add(thread);
            printState(thread.getName() + " is inside. Reading for " + readingTime + " ms.");
        }


    }

    /**
     * Reader exit protocol. Releases one resource permit.
     */
    public void stopReading() {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            runningList.remove(thread);
            printState(thread.getName() + " leaves.");
        }
        resourceSemaphore.release(1);
    }

    /**
     * Requests entry for a writer.
     * Acquires the turnstile and all resource permits to ensure exclusive access.
     *
     * @param writingTime Duration in ms for the simulated writing task.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void startWriting(int writingTime) throws InterruptedException {
        Thread thread = Thread.currentThread();

        synchronized (this) {
            waitingList.add(thread);
            printState(thread.getName() + " wants to enter.");
        }

        // Wait in line
        // Once at the front of the queue, wait for all permits to ensure exclusivity
        resourceSemaphore.acquire(MAX_READERS);
        synchronized (this) {
            waitingList.remove(thread);
            runningList.add(thread);
            printState(thread.getName() + " is inside. Writing for " + writingTime + " ms.");
        }

    }

    /**
     * Writer exit protocol. Releases all resource permits back to the library.
     */
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
