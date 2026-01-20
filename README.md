# Readers-Writers Synchronization Project

## Project Overview
This project provides a solution to the classic [**Readers-Writers Problem**](https://en.wikipedia.org/wiki/Readers%E2%80%93writers_problem) using Java's concurrency utilities. The implementation ensures that:
1. Up to **5 concurrent readers** can access the library.
2. **Writers obtain exclusive access**, meaning no other readers or writers can be inside simultaneously.
3. The system follows a **Strict FIFO (First-In-First-Out)** entry order.

## The Dual-Semaphore (Turnstile) Solution

### Why One Semaphore is Insufficient
In a naive implementation using a single `resourceSemaphore` with 5 permits:
* **Writer Starvation:** If the library is full of readers and a writer arrives, the writer must wait. However, if new readers continue to arrive, they might see a free slot (as readers exit one by one) and "jump" the queue ahead of the writer because their request for 1 permit can be fulfilled immediately, while the writer's request for 5 cannot.
* **FIFO Violation:** Even with the `fair` flag set to true, a reader arriving after a writer might still be granted entry if the writer is currently blocked waiting for a full set of permits.

### The Turnstile Pattern Implementation
To solve these issues, the project uses two semaphores:
1. **`queueSemaphore` (The Turnstile):** A fair binary semaphore (1 permit). It acts as a single-entry gate.
2. **`resourceSemaphore` (The Library):** A fair semaphore with 5 permits managing actual room capacity.

**How it works:**
Every actor (Reader or Writer) must first acquire the `queueSemaphore`.
* A writer holding the turnstile will wait for all 5 resource permits to be freed.
* While the writer is waiting, any newly arriving readers are blocked at the `queueSemaphore`.
* This prevents new readers from "sneaking in" and ensures the writer gets the next available turn, effectively eliminating starvation and enforcing strict FIFO ordering.

## Technical Stack
* **Language:** Java 25
* **Build Tool:** Maven
* **Testing:** JUnit 5, Mockito, AssertJ
* **Quality Assurance:** SonarCube for Code Coverage