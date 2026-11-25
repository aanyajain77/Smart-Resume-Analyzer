package service;

import database.DatabaseManager;
import model.Candidate;
import model.Job;
import model.MatchResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service calculating dynamic matching scores between Candidates and Jobs.
 * Demonstrates:
 * 1. Weighted scoring: Skills (40%), Experience (25%), Education (20%), Projects/Certs (15%)
 * 2. Multidimensional Array: double[][] matchingScores for candidate x job matrix
 * 3. Dynamic comparison and scoring without hardcoded values
 */
public class MatchingService {

    private final RecommendationService recommendationService;

    public MatchingService() {
        this.recommendationService = new RecommendationService();
    }

    public MatchingService(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Performs a comprehensive evaluation of candidate against a job,
     * calculates weighted sub-scores, generates recommendations, and persists result.
     */
    public MatchResult analyze(Candidate candidate, Job job) {
        if (candidate == null || job == null) {
            throw new IllegalArgumentException("Candidate and Job must not be null.");
        }

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        // 1. Technical Skills Score (Max 40.0)
        double skillsScore = calculateSkillsScore(candidate, job, matchedSkills, missingSkills);

        // 2. Work Experience Score (Max 25.0)
        double experienceScore = calculateExperienceScore(candidate, job);

        // 3. Education Score (Max 20.0)
        double educationScore = calculateEducationScore(candidate, job);

        // 4. Projects and Certifications Score (Max 15.0)
        double projectsScore = calculateProjectsAndCertsScore(candidate);

        // Final Percentage (0 to 100)
        double finalScore = round(skillsScore + experienceScore + educationScore + projectsScore, 2);
        finalScore = Math.min(100.0, Math.max(0.0, finalScore));

        // Recommendations
        List<String> recs = recommendationService.generateRecommendations(candidate, job, missingSkills);

        MatchResult result = new MatchResult(
            candidate, job,
            round(skillsScore, 2),
            round(experienceScore, 2),
            round(educationScore, 2),
            round(projectsScore, 2),
            finalScore,
            matchedSkills,
            missingSkills,
            recs
        );

        // Persist to Database
        DatabaseManager.insertMatchResult(result);

        return result;
    }

    /**
     * Multidimensional array demonstration:
     * Calculates and populates matchingScores[candidateIndex][jobIndex]
     */
    public double[][] calculateMatchingMatrix(List<Candidate> candidates, List<Job> jobs) {
        if (candidates == null || jobs == null || candidates.isEmpty() || jobs.isEmpty()) {
            return new double[0][0];
        }

        // Multidimensional array: double[numCandidates][numJobs]
        double[][] matchingScores = new double[candidates.size()][jobs.size()];

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = 0; j < jobs.size(); j++) {
                MatchResult mr = analyze(candidates.get(i), jobs.get(j));
                matchingScores[i][j] = mr.getFinalScore();
            }
        }

        return matchingScores;
    }

    /**
     * Calculates Technical Skills Score (Weight 40%)
     */
    public double calculateSkillsScore(Candidate candidate, Job job,
                                       List<String> outMatched, List<String> outMissing) {
        List<String> candSkills = candidate.getSkills();
        List<String> reqSkills = job.getRequiredSkills();
        List<String> prefSkills = job.getPreferredSkills();

        Set<String> matchedSet = new LinkedHashSet<>();
        Set<String> missingSet = new LinkedHashSet<>();

        int reqMatched = 0;
        for (String req : reqSkills) {
            if (hasSkill(candSkills, req)) {
                reqMatched++;
                matchedSet.add(req);
            } else {
                missingSet.add(req);
            }
        }

        int prefMatched = 0;
        for (String pref : prefSkills) {
            if (hasSkill(candSkills, pref)) {
                prefMatched++;
                matchedSet.add(pref + " (Preferred)");
            }
        }

        outMatched.addAll(matchedSet);
        outMissing.addAll(missingSet);

        if (reqSkills.isEmpty() && prefSkills.isEmpty()) {
            return 40.0;
        }

        double score;
        if (prefSkills.isEmpty()) {
            score = reqSkills.isEmpty() ? 40.0 : ((double) reqMatched / reqSkills.size()) * 40.0;
        } else {
            // Required skills: up to 30 pts; Preferred skills: up to 10 pts
            double reqScore = reqSkills.isEmpty() ? 30.0 : ((double) reqMatched / reqSkills.size()) * 30.0;
            double prefScore = ((double) prefMatched / prefSkills.size()) * 10.0;
            score = reqScore + prefScore;
        }

        return Math.min(40.0, Math.max(0.0, score));
    }

    private boolean hasSkill(List<String> candidateSkills, String target) {
        if (candidateSkills == null || target == null) return false;
        String t = target.trim().toLowerCase();
        for (String s : candidateSkills) {
            if (s.equalsIgnoreCase(t) || s.toLowerCase().contains(t) || t.contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates Work Experience Score (Weight 25%)
     */
    public double calculateExperienceScore(Candidate candidate, Job job) {
        double required = job.getMinimumExperience();
        double actual = candidate.getExperienceYears();

        if (required <= 0.0) return 25.0;
        if (actual >= required) return 25.0;

        return Math.min(25.0, (actual / required) * 25.0);
    }

    /**
     * Calculates Education Score (Weight 20%)
     */
    public double calculateEducationScore(Candidate candidate, Job job) {
        int candLevel = getEduLevel(candidate.getEducation());
        int jobLevel = getEduLevel(job.getRequiredEducation());

        if (candLevel >= jobLevel) {
            return 20.0;
        } else if (candLevel == jobLevel - 1) {
            return 13.0; // One level lower
        } else if (candLevel == jobLevel - 2) {
            return 7.0;  // Two levels lower
        } else {
            return candLevel > 0 ? 3.0 : 0.0;
        }
    }

    private int getEduLevel(String edu) {
        if (edu == null || edu.trim().isEmpty()) return 0;
        String lower = edu.toLowerCase();
        if (lower.contains("ph.d") || lower.contains("phd") || lower.contains("doctorate")) return 5;
        if (lower.contains("master") || lower.contains("m.s") || lower.contains("m.tech") ||
            lower.contains("mba") || lower.contains("mca")) return 4;
        if (lower.contains("bachelor") || lower.contains("b.tech") || lower.contains("btech") ||
            lower.contains("b.e") || lower.contains("bca") || lower.contains("b.s")) return 3;
        if (lower.contains("diploma") || lower.contains("associate")) return 2;
        return 1;
    }

    /**
     * Calculates Projects & Certifications Score (Weight 15%)
     */
    public double calculateProjectsAndCertsScore(Candidate candidate) {
        int numProjects = candidate.getProjects() != null ? candidate.getProjects().size() : 0;
        int numCerts = candidate.getCertifications() != null ? candidate.getCertifications().size() : 0;

        double projPts = (numProjects >= 2) ? 8.0 : (numProjects == 1 ? 5.0 : 0.0);
        double certPts = (numCerts >= 2) ? 7.0 : (numCerts == 1 ? 4.0 : 0.0);

        return Math.min(15.0, projPts + certPts);
    }

    private double round(double val, int places) {
        BigDecimal bd = BigDecimal.valueOf(val);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
