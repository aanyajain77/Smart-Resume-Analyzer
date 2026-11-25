package multithreading;

import model.Candidate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Coordinator class managing Multithreaded Resume Processing.
 * Demonstrates:
 * 1. Thread lifecycle management: NEW, RUNNABLE, TIMED_WAITING, WAITING, TERMINATED
 * 2. start(), join(), sleep(), interrupt()
 * 3. Daemon threads for background queue monitoring
 * 4. Thread priorities (Thread.MAX_PRIORITY, NORM_PRIORITY, MIN_PRIORITY)
 * 5. Synchronization using wait(), notify(), and notifyAll()
 * 6. Shared LinkedList queue and thread-safe results collection
 */
public class ResumeProcessor {

    private final LinkedList<File> queue;
    private final int capacity;
    private boolean producerFinished;
    private final List<Candidate> processedCandidates;

    public ResumeProcessor(int capacity) {
        this.capacity = capacity > 0 ? capacity : 10;
        this.queue = new LinkedList<>();
        this.producerFinished = false;
        this.processedCandidates = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Synchronized method demonstrating wait() and notifyAll().
     * Called by ResumeProducer.
     */
    public synchronized void enqueue(File file) throws InterruptedException {
        // Wait while queue is full
        while (queue.size() >= capacity) {
            wait();
        }
        queue.addLast(file);
        // Notify waiting consumers that an item is available
        notifyAll();
    }

    /**
     * Synchronized method demonstrating wait() and notifyAll().
     * Called by ResumeConsumer.
     */
    public synchronized File dequeue() throws InterruptedException {
        // Wait while queue is empty and producer is still working
        while (queue.isEmpty() && !producerFinished) {
            wait();
        }

        if (queue.isEmpty() && producerFinished) {
            return null; // Signals consumers to terminate
        }

        File file = queue.removeFirst();
        // Notify producer that space is available in the queue
        notifyAll();
        return file;
    }

    public synchronized void setProducerFinished(boolean finished) {
        this.producerFinished = finished;
        // Wake up any waiting consumers so they can check termination condition
        notifyAll();
    }

    public synchronized int getQueueSize() {
        return queue.size();
    }

    public void addProcessedCandidate(Candidate c) {
        // Thread-safe addition to synchronized list
        processedCandidates.add(c);
    }

    public List<Candidate> getProcessedCandidates() {
        return new ArrayList<>(processedCandidates);
    }

    /**
     * Executes the multithreaded processing pipeline.
     * Demonstrates start(), join(), sleep(), daemon threads, and priorities.
     */
    public List<Candidate> processBatch(List<File> files) {
        processedCandidates.clear();
        producerFinished = false;

        System.out.println("\n" + "=".repeat(75));
        System.out.println("       STARTING MULTITHREADED RESUME PROCESSING PIPELINE");
        System.out.println("=".repeat(75));

        // 1. Create Producer Thread with higher priority
        ResumeProducer producer = new ResumeProducer(this, files);
        Thread producerThread = new Thread(producer, "Producer-Thread");
        producerThread.setPriority(Thread.NORM_PRIORITY + 1);

        // 2. Create Consumer Threads
        Thread consumer1 = new Thread(new ResumeConsumer(this, 1), "Consumer-Thread-1");
        Thread consumer2 = new Thread(new ResumeConsumer(this, 2), "Consumer-Thread-2");
        consumer1.setPriority(Thread.NORM_PRIORITY);
        consumer2.setPriority(Thread.NORM_PRIORITY);

        // 3. Create Daemon Monitor Thread
        Thread daemonMonitor = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(250);
                    // Daemon thread logs queue health in background
                }
            } catch (InterruptedException e) {
                // Daemon cleanly terminates
            }
        }, "Queue-Monitor-Daemon");
        daemonMonitor.setDaemon(true); // Daemon thread demonstration!

        // Demonstrate Thread Lifecycle: NEW state
        System.out.printf("Initial Thread States: %s=%s | %s=%s | %s=%s\n",
                producerThread.getName(), producerThread.getState(),
                consumer1.getName(), consumer1.getState(),
                daemonMonitor.getName(), daemonMonitor.getState());

        // Start all threads (RUNNABLE state)
        daemonMonitor.start();
        consumer1.start();
        consumer2.start();
        producerThread.start();

        try {
            // Join threads: Wait for completion
            producerThread.join();
            consumer1.join();
            consumer2.join();
            // Daemon thread ends automatically when user threads finish
        } catch (InterruptedException e) {
            System.err.println("[Thread Error] Pipeline interrupted: " + e.getMessage());
            producerThread.interrupt();
            consumer1.interrupt();
            consumer2.interrupt();
            Thread.currentThread().interrupt();
        }

        System.out.println("=".repeat(75));
        System.out.printf("Pipeline Completed! Total Candidates Extracted & Saved: %d\n", processedCandidates.size());
        System.out.println("=".repeat(75) + "\n");

        return getProcessedCandidates();
    }
}
