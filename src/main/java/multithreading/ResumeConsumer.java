package multithreading;

import database.DatabaseManager;
import exceptions.DuplicateCandidateException;
import model.Candidate;
import service.ResumeParser;
import service.TxtResumeParser;

import java.io.File;

/**
 * Consumer thread in Producer-Consumer pattern.
 * Demonstrates:
 * 1. Runnable implementation
 * 2. Dequeuing and parsing resume files concurrently
 * 3. Synchronization protection when updating shared data
 * 4. Exception handling and thread interruption check
 */
public class ResumeConsumer implements Runnable {

    private final ResumeProcessor processor;
    private final ResumeParser parser;
    private final int consumerId;

    public ResumeConsumer(ResumeProcessor processor, int consumerId) {
        this.processor = processor;
        this.parser = new TxtResumeParser();
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Consumer-%d active and ready.\n",
                Thread.currentThread().getName(), consumerId);

        try {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.printf("[%s] Consumer-%d interrupted.\n", Thread.currentThread().getName(), consumerId);
                    break;
                }

                // Dequeue file (uses wait() and notifyAll() in processor)
                File file = processor.dequeue();
                if (file == null) {
                    // Queue empty and producer finished
                    break;
                }

                System.out.printf("  [Queue -> Consumer-%d] Processing: %s\n", consumerId, file.getName());

                try {
                    Candidate candidate = parser.parse(file);

                    // Check for duplicate in database before inserting
                    if (!DatabaseManager.candidateExistsByEmail(candidate.getEmail())) {
                        int id = DatabaseManager.insertCandidate(candidate);
                        candidate.setCandidateId(id);
                        processor.addProcessedCandidate(candidate);
                        System.out.printf("    [Consumer-%d OK] Extracted & Saved: %s (ID #%d)\n",
                                consumerId, candidate.getName(), id);
                    } else {
                        System.out.printf("    [Consumer-%d SKIP] Duplicate candidate email '%s'.\n",
                                consumerId, candidate.getEmail());
                    }
                } catch (DuplicateCandidateException e) {
                    System.out.printf("    [Consumer-%d SKIP] %s\n", consumerId, e.getMessage());
                } catch (Exception e) {
                    System.out.printf("    [Consumer-%d ERROR] %s: %s\n", consumerId, file.getName(), e.getMessage());
                }

                // Brief thread pause to allow interleaving
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.printf("[%s] Consumer-%d interrupted during wait/sleep.\n",
                    Thread.currentThread().getName(), consumerId);
            Thread.currentThread().interrupt();
        } finally {
            System.out.printf("[%s] Consumer-%d finished.\n", Thread.currentThread().getName(), consumerId);
        }
    }
}
