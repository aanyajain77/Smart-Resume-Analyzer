package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the complete evaluation of a Candidate against a Job Description.
 * Follows the syllabus weighted scoring formula:
 * - Technical Skills: 40%
 * - Work Experience: 25%
 * - Education: 20%
 * - Projects & Certifications: 15%
 * Total: 100%
 */
public class MatchResult {
    private int matchId;
    private Candidate candidate;
    private Job job;
    private double skillsScore;        // Max 40.0
    private double experienceScore;    // Max 25.0
    private double educationScore;     // Max 20.0
    private double projectsScore;      // Max 15.0
    private double finalScore;         // Max 100.0
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> recommendations;

    public MatchResult() {
        this.matchedSkills = new ArrayList<>();
        this.missingSkills = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    public MatchResult(Candidate candidate, Job job, double skillsScore,
                       double experienceScore, double educationScore, double projectsScore,
                       double finalScore, List<String> matchedSkills,
                       List<String> missingSkills, List<String> recommendations) {
        this.candidate = candidate;
        this.job = job;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.projectsScore = projectsScore;
        this.finalScore = finalScore;
        this.matchedSkills = matchedSkills != null ? new ArrayList<>(matchedSkills) : new ArrayList<>();
        this.missingSkills = missingSkills != null ? new ArrayList<>(missingSkills) : new ArrayList<>();
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public MatchResult(int matchId, Candidate candidate, Job job, double skillsScore,
                       double experienceScore, double educationScore, double projectsScore,
                       double finalScore, List<String> matchedSkills,
                       List<String> missingSkills, List<String> recommendations) {
        this(candidate, job, skillsScore, experienceScore, educationScore, projectsScore,
             finalScore, matchedSkills, missingSkills, recommendations);
        this.matchId = matchId;
    }

    // Getters and Setters
    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public double getSkillsScore() {
        return skillsScore;
    }

    public void setSkillsScore(double skillsScore) {
        this.skillsScore = skillsScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public double getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(double educationScore) {
        this.educationScore = educationScore;
    }

    public double getProjectsScore() {
        return projectsScore;
    }

    public void setProjectsScore(double projectsScore) {
        this.projectsScore = projectsScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public List<String> getMatchedSkills() {
        return new ArrayList<>(matchedSkills);
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills != null ? new ArrayList<>(matchedSkills) : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return new ArrayList<>(missingSkills);
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? new ArrayList<>(missingSkills) : new ArrayList<>();
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public String getMatchTier() {
        if (finalScore >= 85.0) return "Strong Match (Top Priority)";
        if (finalScore >= 70.0) return "Good Match (Recommended)";
        if (finalScore >= 50.0) return "Moderate Match (Consider with Training)";
        return "Low Match (Not Recommended)";
    }

    @Override
    public String toString() {
        return String.format(
            "MatchResult [Candidate='%s', Job='%s', FinalScore=%.2f%%, Skills=%.1f/40, Exp=%.1f/25, Edu=%.1f/20, Proj=%.1f/15]",
            (candidate != null ? candidate.getName() : "N/A"),
            (job != null ? job.getJobTitle() : "N/A"),
            finalScore, skillsScore, experienceScore, educationScore, projectsScore
        );
    }
}
