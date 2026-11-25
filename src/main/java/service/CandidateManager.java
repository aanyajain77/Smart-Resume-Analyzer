package service;

import database.DatabaseManager;
import exceptions.CandidateNotFoundException;
import exceptions.DuplicateCandidateException;
import model.Candidate;

import java.sql.SQLException;
import java.util.*;

/**
 * Service managing in-memory candidate collections and database synchronization.
 * Demonstrates:
 * 1. ArrayList for ordered list storage
 * 2. HashMap for constant-time ID and email retrieval
 * 3. Iterator and ListIterator for traversing and modifying collections
 * 4. Comparable (natural ID sorting) and Comparator (custom sorting by name, experience)
 */
public class CandidateManager {

    private final List<Candidate> candidateList;
    private final Map<Integer, Candidate> candidateMapById;
    private final Map<String, Candidate> candidateMapByEmail;

    public CandidateManager() {
        this.candidateList = new ArrayList<>();
        this.candidateMapById = new HashMap<>();
        this.candidateMapByEmail = new HashMap<>();
        refreshFromDatabase();
    }

    /**
     * Synchronizes in-memory collections with the database.
     */
    public synchronized void refreshFromDatabase() {
        candidateList.clear();
        candidateMapById.clear();
        candidateMapByEmail.clear();

        List<Candidate> dbCandidates = DatabaseManager.getAllCandidates();
        for (Candidate c : dbCandidates) {
            candidateList.add(c);
            candidateMapById.put(c.getCandidateId(), c);
            candidateMapByEmail.put(c.getEmail().toLowerCase(), c);
        }
    }

    /**
     * Adds candidate to database and updates in-memory collections.
     */
    public int addCandidate(Candidate candidate) throws DuplicateCandidateException, SQLException {
        if (candidateMapByEmail.containsKey(candidate.getEmail().toLowerCase())) {
            throw new DuplicateCandidateException("Candidate with email '" + candidate.getEmail() + "' already exists in memory.");
        }

        int id = DatabaseManager.insertCandidate(candidate);
        if (id > 0) {
            candidate.setCandidateId(id);
            candidateList.add(candidate);
            candidateMapById.put(id, candidate);
            candidateMapByEmail.put(candidate.getEmail().toLowerCase(), candidate);
        }
        return id;
    }

    /**
     * Returns all candidates.
     */
    public List<Candidate> getAllCandidates() {
        refreshFromDatabase();
        return new ArrayList<>(candidateList);
    }

    /**
     * Searches candidate using ID (HashMap lookup).
     */
    public Candidate searchById(int id) throws CandidateNotFoundException {
        Candidate c = candidateMapById.get(id);
        if (c != null) return c;
        // Fallback to database
        return DatabaseManager.getCandidateById(id);
    }

    /**
     * Searches candidates by Name using Iterator.
     */
    public List<Candidate> searchByName(String nameKeyword) {
        List<Candidate> matches = new ArrayList<>();
        if (nameKeyword == null || nameKeyword.trim().isEmpty()) return matches;

        String lower = nameKeyword.trim().toLowerCase();
        // Demonstrating Iterator
        Iterator<Candidate> iterator = candidateList.iterator();
        while (iterator.hasNext()) {
            Candidate c = iterator.next();
            if (c.getName().toLowerCase().contains(lower)) {
                matches.add(c);
            }
        }
        return matches;
    }

    /**
     * Searches candidate by Email using HashMap lookup.
     */
    public Candidate searchByEmail(String email) {
        if (email == null) return null;
        return candidateMapByEmail.get(email.trim().toLowerCase());
    }

    /**
     * Updates candidate details in database and collections.
     * Demonstrates ListIterator to replace elements during iteration.
     */
    public boolean updateCandidate(Candidate candidate) throws CandidateNotFoundException {
        if (!candidateMapById.containsKey(candidate.getCandidateId())) {
            throw new CandidateNotFoundException("Candidate ID #" + candidate.getCandidateId() + " not found for update.");
        }

        boolean dbOk = DatabaseManager.updateCandidate(candidate);
        if (dbOk) {
            // Update in ArrayList using ListIterator
            ListIterator<Candidate> listIterator = candidateList.listIterator();
            while (listIterator.hasNext()) {
                Candidate current = listIterator.next();
                if (current.getCandidateId() == candidate.getCandidateId()) {
                    listIterator.set(candidate); // Replace element in list
                    break;
                }
            }
            candidateMapById.put(candidate.getCandidateId(), candidate);
            candidateMapByEmail.put(candidate.getEmail().toLowerCase(), candidate);
            return true;
        }
        return false;
    }

    /**
     * Deletes candidate from database and collections using Iterator.remove().
     */
    public boolean deleteCandidate(int candidateId) throws CandidateNotFoundException {
        boolean dbDeleted = DatabaseManager.deleteCandidate(candidateId);
        if (dbDeleted) {
            // Demonstrating safe removal with Iterator
            Iterator<Candidate> iterator = candidateList.iterator();
            while (iterator.hasNext()) {
                Candidate c = iterator.next();
                if (c.getCandidateId() == candidateId) {
                    iterator.remove(); // Safely remove via iterator
                    candidateMapByEmail.remove(c.getEmail().toLowerCase());
                    break;
                }
            }
            candidateMapById.remove(candidateId);
            return true;
        }
        throw new CandidateNotFoundException("Candidate ID #" + candidateId + " could not be deleted.");
    }

    // ==========================================
    // CUSTOM SORTING USING COMPARATOR & COMPARABLE
    // ==========================================

    /**
     * Natural sorting via Comparable (sorted by candidateId).
     */
    public List<Candidate> getCandidatesSortedById() {
        List<Candidate> list = getAllCandidates();
        Collections.sort(list); // Uses Candidate.compareTo()
        return list;
    }

    /**
     * Custom sorting by Work Experience descending using Comparator.
     */
    public List<Candidate> getCandidatesSortedByExperience() {
        List<Candidate> list = getAllCandidates();
        list.sort(new Comparator<Candidate>() {
            @Override
            public int compare(Candidate c1, Candidate c2) {
                return Double.compare(c2.getExperienceYears(), c1.getExperienceYears());
            }
        });
        return list;
    }

    /**
     * Custom sorting by Name alphabetically using Comparator & Lambda.
     */
    public List<Candidate> getCandidatesSortedByName() {
        List<Candidate> list = getAllCandidates();
        list.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return list;
    }

    /**
     * Prints a formatted table of candidates.
     */
    public void displayCandidatesTable(List<Candidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            System.out.println("[!] No candidates found in records.");
            return;
        }

        System.out.println("=".repeat(105));
        System.out.printf("%-5s | %-22s | %-25s | %-12s | %-8s | %-20s\n",
                "ID", "Candidate Name", "Email", "Phone", "Exp(Yrs)", "Education");
        System.out.println("-".repeat(105));

        for (Candidate c : candidates) {
            System.out.printf("%-5d | %-22s | %-25s | %-12s | %-8.1f | %-20s\n",
                    c.getCandidateId(),
                    truncate(c.getName(), 22),
                    truncate(c.getEmail(), 25),
                    truncate(c.getPhone(), 12),
                    c.getExperienceYears(),
                    truncate(c.getEducation(), 20));
        }
        System.out.println("=".repeat(105));
        System.out.printf("Total Candidates: %d\n", candidates.size());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
