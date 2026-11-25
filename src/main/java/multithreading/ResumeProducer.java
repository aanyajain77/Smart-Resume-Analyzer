package multithreading;

import java.io.File;
import java.util.List;

/**
 * Producer thread in Producer-Consumer pattern.
 * Demonstrates:
 * 1. Runnable interface implementation
 * 2. Enqueuing resume files to shared queue
 * 3. Thread.sleep()
 * 4. Proper thread interruption handling
 */
public class ResumeProducer implements Runnable {

    private final ResumeProcessor processor;
    private final List<File> filesToProcess;

    public ResumeProducer(ResumeProcessor processor, List<File> filesToProcess) {
        this.processor = processor;
        this.filesToProcess = filesToProcess;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Producer started. Submitting %d files to queue...\n",
                Thread.currentThread().getName(), filesToProcess.size());

        try {
            for (File file : filesToProcess) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.printf("[%s] Producer was interrupted. Halting submission.\n", Thread.currentThread().getName());
                    break;
                }

                // Add to shared thread-safe queue (uses wait() and notifyAll())
                processor.enqueue(file);
                System.out.printf("  [Producer -> Queue] Added: %s\n", file.getName());

                // Simulate brief I/O delay using Thread.sleep()
                Thread.sleep(80);
            }
        } catch (InterruptedException e) {
            System.out.printf("[%s] Producer interrupted during sleep.\n", Thread.currentThread().getName());
            Thread.currentThread().interrupt(); // Restore interrupted status
        } finally {
            processor.setProducerFinished(true);
            System.out.printf("[%s] Producer completed all submissions.\n", Thread.currentThread().getName());
        }
    }
}
