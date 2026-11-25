package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Candidate for job matching.
 * Demonstrates:
 * 1. Inheritance (extends Person)
 * 2. super keyword in constructors
 * 3. Polymorphism / Method Overriding (displayDetails() and toString())
 * 4. Implementation of Comparable<Candidate> for natural sorting
 * 5. Encapsulation (private fields with controlled access)
 */
public class Candidate extends Person implements Comparable<Candidate> {
    private int candidateId;
    private String education;
    private double experienceYears;
    private List<String> skills;
    private List<String> projects;
    private List<String> certifications;
    private String resumePath;

    // Default Constructor
    public Candidate() {
        super();
        this.skills = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.certifications = new ArrayList<>();
        this.resumePath = "";
        this.education = "";
        this.experienceYears = 0.0;
    }

    // Parameterized Constructor without ID
    public Candidate(String name, String email, String phone, String education,
                     double experienceYears, List<String> skills, List<String> projects,
                     List<String> certifications, String resumePath) {
        super(name, email, phone);
        this.education = education != null ? education.trim() : "";
        this.experienceYears = Math.max(0.0, experienceYears);
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.projects = projects != null ? new ArrayList<>(projects) : new ArrayList<>();
        this.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>();
        this.resumePath = resumePath != null ? resumePath.trim() : "";
    }

    // Parameterized Constructor with ID (Constructor Overloading)
    public Candidate(int candidateId, String name, String email, String phone, String education,
                     double experienceYears, List<String> skills, List<String> projects,
                     List<String> certifications, String resumePath) {
        this(name, email, phone, education, experienceYears, skills, projects, certifications, resumePath);
        this.candidateId = candidateId;
    }

    // Getters and Setters
    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education != null ? education.trim() : "";
    }

    public double getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(double experienceYears) {
        this.experienceYears = Math.max(0.0, experienceYears);
    }

    public List<String> getSkills() {
        return new ArrayList<>(skills);
    }

    public void setSkills(List<String> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public List<String> getProjects() {
        return new ArrayList<>(projects);
    }

    public void setProjects(List<String> projects) {
        this.projects = projects != null ? new ArrayList<>(projects) : new ArrayList<>();
    }

    public List<String> getCertifications() {
        return new ArrayList<>(certifications);
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>();
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath != null ? resumePath.trim() : "";
    }

    // Comma-separated conversion helpers
    public String getSkillsCommaSeparated() {
        return String.join(", ", skills);
    }

    public void setSkillsFromCommaSeparated(String text) {
        this.skills = new ArrayList<>();
        if (text != null && !text.trim().isEmpty()) {
            Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.skills::add);
        }
    }

    public String getProjectsJoined() {
        return String.join(" | ", projects);
    }

    public void setProjectsFromJoined(String text) {
        this.projects = new ArrayList<>();
        if (text != null && !text.trim().isEmpty()) {
            Arrays.stream(text.split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.projects::add);
        }
    }

    public String getCertificationsJoined() {
        return String.join(" | ", certifications);
    }

    public void setCertificationsFromJoined(String text) {
        this.certifications = new ArrayList<>();
        if (text != null && !text.trim().isEmpty()) {
            Arrays.stream(text.split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.certifications::add);
        }
    }

    // Polymorphism: Overriding abstract method from Person
    @Override
    public void displayDetails() {
        System.out.println("--------------------------------------------------");
        System.out.printf("Candidate ID : #%d\n", candidateId);
        System.out.printf("Name         : %s\n", getName());
        System.out.printf("Email        : %s\n", getEmail());
        System.out.printf("Phone        : %s\n", getPhone().isEmpty() ? "N/A" : getPhone());
        System.out.printf("Education    : %s\n", education);
        System.out.printf("Experience   : %.1f years\n", experienceYears);
        System.out.printf("Skills (%d)  : %s\n", skills.size(), getSkillsCommaSeparated());
        System.out.printf("Projects     : %s\n", projects.isEmpty() ? "None" : getProjectsJoined());
        System.out.printf("Certs        : %s\n", certifications.isEmpty() ? "None" : getCertificationsJoined());
        System.out.printf("Resume File  : %s\n", resumePath.isEmpty() ? "None" : resumePath);
        System.out.println("--------------------------------------------------");
    }

    // Natural ordering: Comparable implementation sorted by candidate ID
    @Override
    public int compareTo(Candidate other) {
        if (other == null) return 1;
        return Integer.compare(this.candidateId, other.candidateId);
    }

    @Override
    public String toString() {
        return String.format(
            "Candidate [ID=%d, Name='%s', Email='%s', Exp=%.1f yrs, Edu='%s', Skills=%d]",
            candidateId, getName(), getEmail(), experienceYears, education, skills.size()
        );
    }
}
