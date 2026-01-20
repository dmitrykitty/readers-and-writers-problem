# Readers-Writers Synchronization Project

## Project Overview
This project provides a modern solution to the classic [**Readers-Writers Problem**](https://en.wikipedia.org/wiki/Readers%E2%80%93writers_problem) using Java's latest concurrency utilities. The implementation ensures that:
1. Up to **5 concurrent readers** can access the library.
2. **Writers obtain exclusive access**, meaning no other readers or writers can be inside simultaneously.
3. The system enforces **Strict FIFO (First-In-First-Out)** entry order and prevents starvation.

## Implementation Strategy: Single Fair Semaphore
The current implementation successfully achieves synchronization and starvation prevention using a single **Fair Semaphore**.

### Why a Single Semaphore Works here:
* **Fair Policy:** The `resourceSemaphore` is initialized with `fair = true`. In Java, this ensures that threads are granted permits in the exact order they requested them (FIFO).
* **Starvation Prevention:** When a writer requests 5 permits (`MAX_READERS`), it blocks at the head of the queue. Even if some permits are free, new readers requesting 1 permit cannot "jump" the queue because the fair policy respects the writer's position at the front.
* **Virtual Threads Integration:** By utilizing **Java 25 Virtual Threads**, the system handles thousands of concurrent actors with minimal overhead. Since virtual threads are daemons, the `LibraryRunner` utilizes `t.join()` to ensure the simulation continues until all tasks are processed.

### Alternative Solution: The Turnstile Pattern
While this project demonstrates the effectiveness of a single fair semaphore, a classical academic solution involves a **Dual-Semaphore (Turnstile)** pattern.
* That approach uses an additional binary semaphore (`queueSemaphore`) to act as a gate, ensuring threads "check in" before attempting to acquire the resource.
* Although not strictly necessary in this Java implementation due to the robust "fair" policy of `java.util.concurrent.Semaphore`, the Turnstile pattern remains a significant theoretical alternative for systems where semaphore fairness is not guaranteed by the runtime environment.

## Technical Stack
* **Language:** Java 25
* **Concurrency:** Virtual Threads
* **Build Tool:** Maven
* **Testing:** JUnit 5, Mockito, AssertJ
* **Quality Assurance:** SonarCube & JaCoCo for Code Coverage