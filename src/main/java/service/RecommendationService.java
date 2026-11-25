package service;

import model.Candidate;
import model.Job;

import java.util.*;

/**
 * Service that identifies missing skill gaps and provides targeted recommendations.
 * Demonstrates:
 * 1. HashSet to identify unique skills
 * 2. LinkedHashSet to preserve encounter order
 * 3. TreeSet to display skills alphabetically
 */
public class RecommendationService {

    private static final Map<String, String> SKILL_ROADMAP = new HashMap<>();

    static {
        SKILL_ROADMAP.put("Java", "Master Modern Java (17/21): OOP, Concurrency, Streams API, Records, and JVM internals.");
        SKILL_ROADMAP.put("Spring Boot", "Build microservices with Spring Boot 3: REST APIs, Spring Security, Spring Data JPA.");
        SKILL_ROADMAP.put("Hibernate", "Deepen ORM understanding: Entity mappings, caching, JPQL optimization, lazy loading.");
        SKILL_ROADMAP.put("React", "Build modern SPAs: React Hooks, state management (Redux/Zustand), and React Router.");
        SKILL_ROADMAP.put("SQL", "Master advanced SQL: Indexing strategies, complex joins, window functions, and transactions.");
        SKILL_ROADMAP.put("MySQL", "Database administration: InnoDB tuning, indexing, and slow query analysis.");
        SKILL_ROADMAP.put("Docker", "Containerization: Multi-stage Dockerfiles, image optimization, and Docker Compose.");
        SKILL_ROADMAP.put("Kubernetes", "Container Orchestration: Pods, Services, Deployments, ConfigMaps, and Helm charts.");
        SKILL_ROADMAP.put("AWS", "Cloud Computing: AWS Certified Solutions Architect topics (EC2, S3, RDS, Lambda, IAM).");
        SKILL_ROADMAP.put("Kafka", "Event Streaming: Producer/consumer patterns, topics, partitions, and Kafka Streams.");
        SKILL_ROADMAP.put("Python", "Production Python: Advanced OOP, asynchronous programming (asyncio), and virtual environments.");
        SKILL_ROADMAP.put("Machine Learning", "Applied ML: Supervised/unsupervised algorithms, evaluation metrics with Scikit-learn.");
    }

    /**
     * Generates a list of actionable learning recommendations.
     */
    public List<String> generateRecommendations(Candidate candidate, Job job, List<String> missingSkills) {
        List<String> recs = new ArrayList<>();

        if (missingSkills != null && !missingSkills.isEmpty()) {
            // TreeSet to sort missing skills alphabetically
            Set<String> sortedMissing = new TreeSet<>(missingSkills);
            for (String skill : sortedMissing) {
                String guidance = SKILL_ROADMAP.get(skill);
                if (guidance != null) {
                    recs.add(String.format("Target '%s': %s", skill, guidance));
                } else {
                    recs.add(String.format("Target '%s': Study official documentation and build a hands-on project to demonstrate proficiency.", skill));
                }
            }
        } else {
            recs.add("Excellent! Candidate possesses all required technical skills for this role.");
        }

        // Experience Gap Check
        if (candidate.getExperienceYears() < job.getMinimumExperience()) {
            double gap = job.getMinimumExperience() - candidate.getExperienceYears();
            recs.add(String.format("Experience Gap: Candidate has %.1f years vs %.1f years required (Gap: %.1f yrs). Consider relevant open-source contributions or internship experience.",
                    candidate.getExperienceYears(), job.getMinimumExperience(), gap));
        }

        // Certifications Check
        if (candidate.getCertifications().isEmpty()) {
            recs.add("Credentials: Earn an industry-standard certification (e.g. Oracle Certified Professional, AWS Solutions Architect) to enhance profile competitiveness.");
        }

        return recs;
    }

    /**
     * Formats and prints the Skill Gap Analysis on console using:
     * - HashSet (unique skills)
     * - LinkedHashSet (preserved order)
     * - TreeSet (alphabetical display)
     */
    public void displaySkillGapAnalysis(Candidate candidate, Job job,
                                       List<String> matchedSkills, List<String> missingSkills,
                                       List<String> recommendations) {
        System.out.println("\n" + "=".repeat(85));
        System.out.println("              SKILL GAP ANALYSIS & RECOMMENDATION REPORT");
        System.out.println("=".repeat(85));
        System.out.printf("Candidate : %s (%s)\n", candidate.getName(), candidate.getEmail());
        System.out.printf("Target Job: %s at %s\n", job.getJobTitle(), job.getCompanyName());
        System.out.println("-".repeat(85));

        // LinkedHashSet for matched skills (preserves insertion order)
        Set<String> orderedMatched = new LinkedHashSet<>(matchedSkills);
        System.out.printf("\n[+] MATCHED SKILLS (%d):\n", orderedMatched.size());
        if (orderedMatched.isEmpty()) {
            System.out.println("    None identified.");
        } else {
            for (String s : orderedMatched) {
                System.out.println("    [MATCH] " + s);
            }
        }

        // TreeSet for missing skills (alphabetical order)
        Set<String> sortedMissing = new TreeSet<>(missingSkills);
        System.out.printf("\n[-] MISSING REQUIRED SKILLS (%d) (Alphabetical):\n", sortedMissing.size());
        if (sortedMissing.isEmpty()) {
            System.out.println("    None! Candidate fulfills all required technical competencies.");
        } else {
            for (String s : sortedMissing) {
                System.out.println("    [MISSING] " + s);
            }
        }

        // HashSet for unique high-impact skills that would improve score
        Set<String> impactSkills = new HashSet<>(missingSkills);
        System.out.printf("\n[*] SKILLS THAT WOULD IMMEDIATELY IMPROVE MATCH SCORE (%d):\n", impactSkills.size());
        for (String s : impactSkills) {
            System.out.printf("    -> %s (+%.1f%% potential increase in technical score)\n",
                    s, (40.0 / Math.max(1, job.getRequiredSkills().size())));
        }

        System.out.println("\n[*] ACTIONABLE LEARNING PATH RECOMMENDATIONS:");
        int idx = 1;
        for (String r : recommendations) {
            System.out.printf("    %d. %s\n", idx++, r);
        }
        System.out.println("=".repeat(85) + "\n");
    }
}
