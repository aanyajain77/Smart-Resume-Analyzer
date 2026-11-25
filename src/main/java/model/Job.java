package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Job Description created by recruiters.
 * Contains qualification thresholds and skills for resume matching.
 */
public class Job {
    private int jobId;
    private String jobTitle;
    private String companyName;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private double minimumExperience;
    private String requiredEducation;
    private String requiredProjectsOrCertifications;

    // Default Constructor
    public Job() {
        this.requiredSkills = new ArrayList<>();
        this.preferredSkills = new ArrayList<>();
        this.jobTitle = "";
        this.companyName = "";
        this.requiredEducation = "";
        this.requiredProjectsOrCertifications = "";
    }

    // Parameterized Constructor without ID
    public Job(String jobTitle, String companyName, List<String> requiredSkills,
               List<String> preferredSkills, double minimumExperience,
               String requiredEducation, String requiredProjectsOrCertifications) {
        this.jobTitle = jobTitle != null ? jobTitle.trim() : "";
        this.companyName = companyName != null ? companyName.trim() : "";
        this.requiredSkills = requiredSkills != null ? new ArrayList<>(requiredSkills) : new ArrayList<>();
        this.preferredSkills = preferredSkills != null ? new ArrayList<>(preferredSkills) : new ArrayList<>();
        this.minimumExperience = Math.max(0.0, minimumExperience);
        this.requiredEducation = requiredEducation != null ? requiredEducation.trim() : "";
        this.requiredProjectsOrCertifications = requiredProjectsOrCertifications != null ?
                requiredProjectsOrCertifications.trim() : "";
    }

    // Parameterized Constructor with ID (Constructor Overloading)
    public Job(int jobId, String jobTitle, String companyName, List<String> requiredSkills,
               List<String> preferredSkills, double minimumExperience,
               String requiredEducation, String requiredProjectsOrCertifications) {
        this(jobTitle, companyName, requiredSkills, preferredSkills, minimumExperience,
             requiredEducation, requiredProjectsOrCertifications);
        this.jobId = jobId;
    }

    // Getters and Setters
    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle != null ? jobTitle.trim() : "";
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName != null ? companyName.trim() : "";
    }

    public List<String> getRequiredSkills() {
        return new ArrayList<>(requiredSkills);
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? new ArrayList<>(requiredSkills) : new ArrayList<>();
    }

    public List<String> getPreferredSkills() {
        return new ArrayList<>(preferredSkills);
    }

    public void setPreferredSkills(List<String> preferredSkills) {
        this.preferredSkills = preferredSkills != null ? new ArrayList<>(preferredSkills) : new ArrayList<>();
    }

    public double getMinimumExperience() {
        return minimumExperience;
    }

    public void setMinimumExperience(double minimumExperience) {
        this.minimumExperience = Math.max(0.0, minimumExperience);
    }

    public String getRequiredEducation() {
        return requiredEducation;
    }

    public void setRequiredEducation(String requiredEducation) {
        this.requiredEducation = requiredEducation != null ? requiredEducation.trim() : "";
    }

    public String getRequiredProjectsOrCertifications() {
        return requiredProjectsOrCertifications;
    }

    public void setRequiredProjectsOrCertifications(String requiredProjectsOrCertifications) {
        this.requiredProjectsOrCertifications = requiredProjectsOrCertifications != null ?
                requiredProjectsOrCertifications.trim() : "";
    }

    // Helper conversion methods
    public String getRequiredSkillsCommaSeparated() {
        return String.join(", ", requiredSkills);
    }

    public void setRequiredSkillsFromCommaSeparated(String text) {
        this.requiredSkills = new ArrayList<>();
        if (text != null && !text.trim().isEmpty()) {
            Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.requiredSkills::add);
        }
    }

    public String getPreferredSkillsCommaSeparated() {
        return String.join(", ", preferredSkills);
    }

    public void setPreferredSkillsFromCommaSeparated(String text) {
        this.preferredSkills = new ArrayList<>();
        if (text != null && !text.trim().isEmpty()) {
            Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.preferredSkills::add);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "Job #%d: %s at %s | Min Exp: %.1f yrs | Required Edu: %s\n  Required Skills : [%s]\n  Preferred Skills: [%s]",
            jobId, jobTitle, companyName, minimumExperience, requiredEducation,
            getRequiredSkillsCommaSeparated(), getPreferredSkillsCommaSeparated()
        );
    }
}
