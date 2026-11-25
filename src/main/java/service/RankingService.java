package service;

import database.DatabaseManager;
import model.Candidate;
import model.Job;
import model.MatchResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service providing candidate ranking, leaderboard presentation, and stream filtering.
 * Demonstrates:
 * 1. Comparable (natural candidate sorting) and Comparator (custom scoring sorting)
 * 2. TreeMap for sorted score-to-candidate maps
 * 3. PriorityQueue for Max-Heap retrieval of top candidates
 * 4. Lambda expressions for comparisons and predicates
 * 5. Stream API: filter(), map(), sorted(), distinct(), limit(), skip(), collect(), forEach()
 */
public class RankingService {

    private final MatchingService matchingService;

    public RankingService(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * Evaluates all candidates against a job and returns sorted MatchResults
     * using Lambda expressions and Comparator.
     */
    public List<MatchResult> rankCandidatesForJob(Job job) {
        List<Candidate> candidates = DatabaseManager.getAllCandidates();
        if (candidates.isEmpty() || job == null) {
            return new ArrayList<>();
        }

        // Stream API: map each candidate to MatchResult, then sort using Lambda
        return candidates.stream()
                .map(c -> matchingService.analyze(c, job))
                .sorted((r1, r2) -> Double.compare(r2.getFinalScore(), r1.getFinalScore()))
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates PriorityQueue (Max-Heap) to retrieve top N candidates.
     */
    public List<MatchResult> getTopNCandidates(Job job, int n) {
        List<Candidate> candidates = DatabaseManager.getAllCandidates();

        // PriorityQueue ordered descending by final score
        PriorityQueue<MatchResult> pq = new PriorityQueue<>(
            (a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore())
        );

        for (Candidate c : candidates) {
            pq.offer(matchingService.analyze(c, job));
        }

        List<MatchResult> topList = new ArrayList<>();
        for (int i = 0; i < n && !pq.isEmpty(); i++) {
            topList.add(pq.poll());
        }
        return topList;
    }

    /**
     * Demonstrates TreeMap for grouping/ranking candidates by score.
     */
    public TreeMap<Double, List<Candidate>> groupCandidatesByScore(Job job) {
        // Reverse order TreeMap (highest score first)
        TreeMap<Double, List<Candidate>> treeMap = new TreeMap<>(Collections.reverseOrder());

        List<Candidate> candidates = DatabaseManager.getAllCandidates();
        for (Candidate c : candidates) {
            MatchResult mr = matchingService.analyze(c, job);
            treeMap.computeIfAbsent(mr.getFinalScore(), k -> new ArrayList<>()).add(c);
        }
        return treeMap;
    }

    /**
     * Filters ranked candidates using Stream API:
     * Demonstrates filter(), map(), sorted(), distinct(), limit(), collect()
     */
    public List<MatchResult> filterCandidates(Job job, double minScore, double minExp,
                                             String requiredSkill, String requiredEdu) {
        List<Candidate> allCandidates = DatabaseManager.getAllCandidates();

        return allCandidates.stream()
                .filter(c -> c.getExperienceYears() >= minExp)
                .filter(c -> {
                    if (requiredEdu == null || requiredEdu.trim().isEmpty()) return true;
                    return c.getEducation().toLowerCase().contains(requiredEdu.toLowerCase());
                })
                .filter(c -> {
                    if (requiredSkill == null || requiredSkill.trim().isEmpty()) return true;
                    return c.getSkills().stream().anyMatch(s -> s.equalsIgnoreCase(requiredSkill.trim()));
                })
                .map(c -> matchingService.analyze(c, job))
                .filter(mr -> mr.getFinalScore() >= minScore)
                .sorted((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Calculates average matching score across all candidates using Streams.
     */
    public double calculateAverageScore(Job job) {
        List<Candidate> candidates = DatabaseManager.getAllCandidates();
        if (candidates.isEmpty()) return 0.0;

        return candidates.stream()
                .mapToDouble(c -> matchingService.analyze(c, job).getFinalScore())
                .average()
                .orElse(0.0);
    }

    /**
     * Finds highest-scoring candidate using Stream API max().
     */
    public Optional<MatchResult> findHighestScoringCandidate(Job job) {
        List<Candidate> candidates = DatabaseManager.getAllCandidates();
        return candidates.stream()
                .map(c -> matchingService.analyze(c, job))
                .max(Comparator.comparingDouble(MatchResult::getFinalScore));
    }

    /**
     * Displays formatted leaderboard on console.
     */
    public void displayLeaderboard(Job job, List<MatchResult> rankedResults) {
        System.out.println("\n" + "=".repeat(95));
        System.out.printf("                CANDIDATE LEADERBOARD FOR: %s (%s)\n", job.getJobTitle(), job.getCompanyName());
        System.out.println("=".repeat(95));

        if (rankedResults.isEmpty()) {
            System.out.println("[!] No candidates found matching criteria.");
            System.out.println("=".repeat(95));
            return;
        }

        System.out.printf("%-5s | %-25s | %-12s | %-15s | %-15s\n",
                "Rank", "Candidate Name", "Experience", "Matching Score", "Suitability Tier");
        System.out.println("-".repeat(95));

        int rank = 1;
        for (MatchResult mr : rankedResults) {
            System.out.printf("#%-4d | %-25s | %-12s | %6.2f%%         | %-15s\n",
                    rank++,
                    truncate(mr.getCandidate().getName(), 25),
                    String.format("%.1f yrs", mr.getCandidate().getExperienceYears()),
                    mr.getFinalScore(),
                    mr.getMatchTier());
        }

        System.out.println("=".repeat(95));
        System.out.printf("Total Evaluated: %d | Average Score: %.2f%%\n",
                rankedResults.size(), calculateAverageScore(job));
        System.out.println("=".repeat(95) + "\n");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
