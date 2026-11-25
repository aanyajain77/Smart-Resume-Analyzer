package main;

import database.DatabaseManager;
import exceptions.*;
import model.Candidate;
import model.Job;
import model.MatchResult;
import multithreading.ResumeProcessor;
import report.ReportGenerator;
import service.*;
import util.DataManager;
import util.InputValidator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Main application class providing the interactive, menu-driven console interface
 * for the SmartHire: Resume Analyzer and Job Matching System.
 *
 * Course: Programming in Java (25CSE0202)
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CandidateManager candidateManager = new CandidateManager();
    private static final JobManager jobManager = new JobManager();
    private static final RecommendationService recommendationService = new RecommendationService();
    private static final MatchingService matchingService = new MatchingService(recommendationService);
    private static final RankingService rankingService = new RankingService(matchingService);
    private static final ResumeParser resumeParser = new TxtResumeParser();
    private static final DataManager<Candidate> candidateDataManager = new DataManager<>("Candidate");

    public static void main(String[] args) {
        // Initialize Database schema
        try {
            DatabaseManager.initializeDatabase();
            candidateManager.refreshFromDatabase();
            jobManager.refreshFromDatabase();
        } catch (Exception e) {
            System.err.println("[Fatal Error] Database initialization error: " + e.getMessage());
            return;
        }

        boolean running = true;
        System.out.println("\nWelcome to SmartHire System - Course Project: Programming in Java (25CSE0202)");

        while (running) {
            printMainMenu();
            String choice = prompt("Enter your choice: ");

            try {
                switch (choice.trim()) {
                    case "1":
                        handleAddCandidateOrResume();
                        break;
                    case "2":
                        handleViewAllCandidates();
                        break;
                    case "3":
                        handleSearchCandidate();
                        break;
                    case "4":
                        handleUpdateCandidate();
                        break;
                    case "5":
                        handleDeleteCandidate();
                        break;
                    case "6":
                        handleAddJobDescription();
                        break;
                    case "7":
                        handleViewAllJobDescriptions();
                        break;
                    case "8":
                        handleAnalyzeResume();
                        break;
                    case "9":
                        handleRecommendMissingSkills();
                        break;
                    case "10":
                        handleRankCandidates();
                        break;
                    case "11":
                        handleViewMatchingReports();
                        break;
                    case "12":
                        handleProcessMultipleResumesMultithreaded();
                        break;
                    case "13":
                        handleImportCandidatesFromCsv();
                        break;
                    case "14":
                        handleExportToCsv();
                        break;
                    case "15":
                        running = false;
                        handleExit();
                        break;
                    default:
                        System.out.println("\n[!] Invalid choice. Please enter a valid number from 1 to 15.\n");
                        break;
                }
            } catch (InvalidInputException | InvalidResumeException | CandidateNotFoundException |
                     JobNotFoundException | DuplicateCandidateException e) {
                System.out.println("\n[APPLICATION ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[UNEXPECTED ERROR] An error occurred: " + e.getMessage());
            }
        }
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("========== SMART HIRE SYSTEM ==========");
        System.out.println();
        System.out.println("1. Add Candidate / Upload Resume");
        System.out.println("2. View All Candidates");
        System.out.println("3. Search Candidate");
        System.out.println("4. Update Candidate Information");
        System.out.println("5. Delete Candidate");
        System.out.println("6. Add Job Description");
        System.out.println("7. View All Job Descriptions");
        System.out.println("8. Analyze Resume Against Job");
        System.out.println("9. Recommend Missing Skills");
        System.out.println("10. Rank Candidates for a Job");
        System.out.println("11. View Matching Reports");
        System.out.println("12. Process Multiple Resumes");
        System.out.println("13. Import Candidates from CSV");
        System.out.println("14. Export Candidates and Reports to CSV");
        System.out.println("15. Exit");
        System.out.println();
    }

    // ========================================================
    // 1. ADD CANDIDATE / UPLOAD RESUME
    // ========================================================
    private static void handleAddCandidateOrResume()
            throws InvalidInputException, InvalidResumeException, DuplicateCandidateException, SQLException {
        System.out.println("\n--- [1] ADD CANDIDATE / UPLOAD RESUME ---");
        System.out.println("Choose Registration Method:");
        System.out.println("  [A] Upload & Parse Resume File (TXT format)");
        System.out.println("  [B] Manual Candidate Entry");
        String opt = prompt("Select Option (A/B): ");

        if (opt.equalsIgnoreCase("A")) {
            String path = prompt("Enter resume file path (e.g. resumes/resume1_alice.txt): ");
            File file = new File(path.trim());
            if (!file.exists() || !file.isFile()) {
                throw new InvalidResumeException("File not found or invalid: " + path);
            }

            try {
                Candidate c = resumeParser.parse(file);
                int id = candidateManager.addCandidate(c);
                candidateDataManager.addRecord(c);
                System.out.printf("\n[SUCCESS] Resume parsed and candidate registered successfully!\n");
                c.displayDetails();
            } catch (IOException e) {
                throw new InvalidResumeException("Failed to read resume file: " + e.getMessage(), e);
            }
        } else if (opt.equalsIgnoreCase("B")) {
            System.out.println("\n--- Manual Candidate Registration ---");
            String name = InputValidator.validateNonEmpty(prompt("Enter Candidate Name: "), "Name");
            String email = InputValidator.validateEmail(prompt("Enter Email: "));
            String phone = InputValidator.validatePhone(prompt("Enter Phone (optional): "));
            String education = InputValidator.validateNonEmpty(prompt("Enter Educational Qualification: "), "Education");

            double exp = 0.0;
            try {
                exp = Double.parseDouble(prompt("Enter Work Experience in years (e.g. 3.5): "));
                InputValidator.validateExperience(exp);
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Experience must be a valid numeric decimal.");
            }

            String skillsStr = InputValidator.validateNonEmpty(prompt("Enter Technical Skills (comma-separated): "), "Skills");
            String projectsStr = prompt("Enter Projects (separated by |): ");
            String certsStr = prompt("Enter Certifications (separated by |): ");

            Candidate c = new Candidate(name, email, phone, education, exp, null, null, null, "Manual Entry");
            c.setSkillsFromCommaSeparated(skillsStr);
            c.setProjectsFromJoined(projectsStr);
            c.setCertificationsFromJoined(certsStr);

            int id = candidateManager.addCandidate(c);
            candidateDataManager.addRecord(c);
            System.out.printf("\n[SUCCESS] Candidate registered successfully with ID #%d!\n", id);
            c.displayDetails();
        } else {
            System.out.println("[!] Invalid option selected.");
        }
    }

    // ========================================================
    // 2. VIEW ALL CANDIDATES
    // ========================================================
    private static void handleViewAllCandidates() {
        System.out.println("\n--- [2] VIEW ALL CANDIDATES ---");
        List<Candidate> candidates = candidateManager.getAllCandidates();

        if (candidates.isEmpty()) {
            System.out.println("[!] No candidates found in system. Please add candidates or upload resumes first.");
            return;
        }

        System.out.println("Sorting Options:");
        System.out.println("  [1] Default / Natural ID Order (Comparable)");
        System.out.println("  [2] Experience: Highest to Lowest (Comparator)");
        System.out.println("  [3] Name: Alphabetical A-Z (Comparator Lambda)");
        String sortChoice = prompt("Select display order (1-3) [Default 1]: ");

        List<Candidate> displayList;
        switch (sortChoice.trim()) {
            case "2":
                displayList = candidateManager.getCandidatesSortedByExperience();
                break;
            case "3":
                displayList = candidateManager.getCandidatesSortedByName();
                break;
            default:
                displayList = candidateManager.getCandidatesSortedById();
                break;
        }

        candidateManager.displayCandidatesTable(displayList);

        String inspect = prompt("\nEnter Candidate ID to view complete profile (or press Enter to return): ");
        if (!inspect.isEmpty()) {
            try {
                int id = Integer.parseInt(inspect);
                Candidate c = candidateManager.searchById(id);
                c.displayDetails();
            } catch (Exception e) {
                System.out.println("[!] " + e.getMessage());
            }
        }
    }

    // ========================================================
    // 3. SEARCH CANDIDATE
    // ========================================================
    private static void handleSearchCandidate() throws CandidateNotFoundException {
        System.out.println("\n--- [3] SEARCH CANDIDATE ---");
        System.out.println("Search By:");
        System.out.println("  [1] Candidate ID (HashMap lookup)");
        System.out.println("  [2] Name Keyword (Collection Iterator)");
        System.out.println("  [3] Email Address (HashMap lookup)");
        String mode = prompt("Enter choice (1-3): ");

        switch (mode.trim()) {
            case "1": {
                int id = promptInt("Enter Candidate ID: ");
                Candidate c = candidateManager.searchById(id);
                c.displayDetails();
                break;
            }
            case "2": {
                String kw = prompt("Enter Name keyword: ");
                List<Candidate> results = candidateManager.searchByName(kw);
                if (results.isEmpty()) {
                    System.out.println("[!] No candidates found matching: " + kw);
                } else {
                    candidateManager.displayCandidatesTable(results);
                }
                break;
            }
            case "3": {
                String email = prompt("Enter Email address: ");
                Candidate c = candidateManager.searchByEmail(email);
                if (c != null) {
                    c.displayDetails();
                } else {
                    System.out.println("[!] No candidate found with email: " + email);
                }
                break;
            }
            default:
                System.out.println("[!] Invalid search criteria.");
                break;
        }
    }

    // ========================================================
    // 4. UPDATE CANDIDATE INFORMATION
    // ========================================================
    private static void handleUpdateCandidate() throws CandidateNotFoundException, InvalidInputException {
        System.out.println("\n--- [4] UPDATE CANDIDATE INFORMATION ---");
        int id = promptInt("Enter Candidate ID to update: ");
        Candidate c = candidateManager.searchById(id);

        System.out.println("\nUpdating Candidate #" + id + " (" + c.getName() + "). Press Enter to keep existing values.");
        String newName = promptOptional("Name [" + c.getName() + "]: ", c.getName());
        String newEmail = promptOptional("Email [" + c.getEmail() + "]: ", c.getEmail());
        String newPhone = promptOptional("Phone [" + c.getPhone() + "]: ", c.getPhone());
        String newEdu = promptOptional("Education [" + c.getEducation() + "]: ", c.getEducation());

        String expStr = promptOptional("Experience [" + c.getExperienceYears() + "]: ", String.valueOf(c.getExperienceYears()));
        double newExp = c.getExperienceYears();
        try {
            newExp = Double.parseDouble(expStr);
            InputValidator.validateExperience(newExp);
        } catch (NumberFormatException ignored) {}

        String newSkills = promptOptional("Skills [" + c.getSkillsCommaSeparated() + "]: ", c.getSkillsCommaSeparated());

        c.setName(newName);
        c.setEmail(newEmail);
        c.setPhone(newPhone);
        c.setEducation(newEdu);
        c.setExperienceYears(newExp);
        c.setSkillsFromCommaSeparated(newSkills);

        // Demonstrates ListIterator in CandidateManager.updateCandidate
        boolean updated = candidateManager.updateCandidate(c);
        if (updated) {
            System.out.println("\n[SUCCESS] Candidate record updated successfully!");
            c.displayDetails();
        } else {
            System.out.println("\n[FAIL] Update could not be persisted.");
        }
    }

    // ========================================================
    // 5. DELETE CANDIDATE
    // ========================================================
    private static void handleDeleteCandidate() throws CandidateNotFoundException {
        System.out.println("\n--- [5] DELETE CANDIDATE ---");
        int id = promptInt("Enter Candidate ID to delete: ");
        Candidate c = candidateManager.searchById(id);

        String confirm = prompt("Are you sure you want to permanently delete '" + c.getName() + "' (ID #" + id + ")? (y/n): ");
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            // Demonstrates Iterator.remove()
            boolean ok = candidateManager.deleteCandidate(id);
            if (ok) {
                System.out.printf("[SUCCESS] Candidate ID #%d (%s) permanently deleted.\n", id, c.getName());
            }
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    // ========================================================
    // 6. ADD JOB DESCRIPTION
    // ========================================================
    private static void handleAddJobDescription() throws InvalidInputException, SQLException {
        System.out.println("\n--- [6] ADD JOB DESCRIPTION ---");
        String title = InputValidator.validateNonEmpty(prompt("Enter Job Title: "), "Job Title");
        String company = InputValidator.validateNonEmpty(prompt("Enter Company Name: "), "Company Name");
        String reqSkills = InputValidator.validateNonEmpty(prompt("Enter Required Skills (comma-separated): "), "Required Skills");
        String prefSkills = prompt("Enter Preferred Skills (comma-separated, optional): ");

        double minExp = 0.0;
        try {
            minExp = Double.parseDouble(prompt("Enter Minimum Experience in years (e.g. 3.0): "));
            InputValidator.validateExperience(minExp);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Minimum experience must be numeric.");
        }

        String education = promptOptional("Enter Required Education [Bachelor's Degree]: ", "Bachelor's Degree");
        String reqProjects = prompt("Enter Required Projects / Certifications: ");

        Job job = new Job(title, company, null, null, minExp, education, reqProjects);
        job.setRequiredSkillsFromCommaSeparated(reqSkills);
        job.setPreferredSkillsFromCommaSeparated(prefSkills);

        int id = jobManager.addJob(job);
        System.out.printf("\n[SUCCESS] Job Description created successfully with ID #%d!\n", id);
        System.out.println(job);
    }

    // ========================================================
    // 7. VIEW ALL JOB DESCRIPTIONS
    // ========================================================
    private static void handleViewAllJobDescriptions() throws JobNotFoundException {
        System.out.println("\n--- [7] ALL JOB DESCRIPTIONS ---");
        List<Job> jobs = jobManager.getAllJobs();

        if (jobs.isEmpty()) {
            System.out.println("[!] No jobs found. Use Option 6 to add a Job Description first.");
            return;
        }

        jobManager.displayJobsTable(jobs);

        System.out.println("\nOptions: [1] View Job Details  [2] Update Job  [3] Delete Job  [4] Return");
        String choice = prompt("Enter choice: ");

        switch (choice.trim()) {
            case "1": {
                int jId = promptInt("Enter Job ID to view: ");
                Job j = jobManager.searchById(jId);
                System.out.println("\n" + "=".repeat(75));
                System.out.println(j);
                System.out.println("=".repeat(75));
                break;
            }
            case "2": {
                int jId = promptInt("Enter Job ID to update: ");
                Job j = jobManager.searchById(jId);
                String nTitle = promptOptional("Job Title [" + j.getJobTitle() + "]: ", j.getJobTitle());
                String nCompany = promptOptional("Company [" + j.getCompanyName() + "]: ", j.getCompanyName());
                String nReq = promptOptional("Required Skills [" + j.getRequiredSkillsCommaSeparated() + "]: ", j.getRequiredSkillsCommaSeparated());
                String nPref = promptOptional("Preferred Skills [" + j.getPreferredSkillsCommaSeparated() + "]: ", j.getPreferredSkillsCommaSeparated());
                String expStr = promptOptional("Min Exp [" + j.getMinimumExperience() + "]: ", String.valueOf(j.getMinimumExperience()));
                double nExp = j.getMinimumExperience();
                try {
                    nExp = Double.parseDouble(expStr);
                } catch (NumberFormatException ignored) {}
                String nEdu = promptOptional("Required Education [" + j.getRequiredEducation() + "]: ", j.getRequiredEducation());

                j.setJobTitle(nTitle);
                j.setCompanyName(nCompany);
                j.setRequiredSkillsFromCommaSeparated(nReq);
                j.setPreferredSkillsFromCommaSeparated(nPref);
                j.setMinimumExperience(nExp);
                j.setRequiredEducation(nEdu);

                jobManager.updateJob(j);
                System.out.println("[SUCCESS] Job Description updated successfully.");
                break;
            }
            case "3": {
                int jId = promptInt("Enter Job ID to delete: ");
                String confirm = prompt("Delete Job #" + jId + "? (y/n): ");
                if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                    jobManager.deleteJob(jId);
                    System.out.println("[SUCCESS] Job deleted.");
                }
                break;
            }
            default:
                break;
        }
    }

    // ========================================================
    // 8. ANALYZE RESUME AGAINST JOB
    // ========================================================
    private static void handleAnalyzeResume() throws CandidateNotFoundException, JobNotFoundException {
        System.out.println("\n--- [8] ANALYZE RESUME AGAINST JOB DESCRIPTION ---");
        List<Candidate> candidates = candidateManager.getAllCandidates();
        List<Job> jobs = jobManager.getAllJobs();

        if (candidates.isEmpty() || jobs.isEmpty()) {
            System.out.println("[!] Both candidates and jobs must exist in the system to perform analysis.");
            return;
        }

        candidateManager.displayCandidatesTable(candidates);
        int cId = promptInt("\nSelect Candidate ID to analyze: ");
        Candidate candidate = candidateManager.searchById(cId);

        jobManager.displayJobsTable(jobs);
        int jId = promptInt("\nSelect Target Job ID: ");
        Job job = jobManager.searchById(jId);

        System.out.println("\nCalculating matching scores dynamically...");
        MatchResult result = matchingService.analyze(candidate, job);
        ReportGenerator.displayReport(result);
    }

    // ========================================================
    // 9. RECOMMEND MISSING SKILLS
    // ========================================================
    private static void handleRecommendMissingSkills() throws CandidateNotFoundException, JobNotFoundException {
        System.out.println("\n--- [9] MISSING SKILLS & CAREER RECOMMENDATIONS ---");
        List<Candidate> candidates = candidateManager.getAllCandidates();
        List<Job> jobs = jobManager.getAllJobs();

        if (candidates.isEmpty() || jobs.isEmpty()) {
            System.out.println("[!] Candidates and jobs must exist to analyze skill gaps.");
            return;
        }

        candidateManager.displayCandidatesTable(candidates);
        int cId = promptInt("\nSelect Candidate ID: ");
        Candidate candidate = candidateManager.searchById(cId);

        jobManager.displayJobsTable(jobs);
        int jId = promptInt("\nSelect Target Job ID: ");
        Job job = jobManager.searchById(jId);

        MatchResult result = matchingService.analyze(candidate, job);
        // Demonstrates HashSet, LinkedHashSet, and TreeSet in RecommendationService
        recommendationService.displaySkillGapAnalysis(
                candidate, job,
                result.getMatchedSkills(),
                result.getMissingSkills(),
                result.getRecommendations()
        );
    }

    // ========================================================
    // 10. RANK CANDIDATES FOR A JOB
    // ========================================================
    private static void handleRankCandidates() throws JobNotFoundException {
        System.out.println("\n--- [10] RANK CANDIDATES FOR A JOB ---");
        List<Job> jobs = jobManager.getAllJobs();
        if (jobs.isEmpty()) {
            System.out.println("[!] No jobs found. Please add a job first (Option 6).");
            return;
        }

        jobManager.displayJobsTable(jobs);
        int jId = promptInt("\nSelect Job ID to rank candidates for: ");
        Job job = jobManager.searchById(jId);

        System.out.println("\nRanking Options:");
        System.out.println("  [1] Standard Leaderboard (All Candidates)");
        System.out.println("  [2] Filter Candidates (Stream API: Score, Exp, Skills, Edu)");
        System.out.println("  [3] Top N Candidates (PriorityQueue Max-Heap)");
        System.out.println("  [4] Group Candidates by Score (TreeMap)");
        System.out.println("  [5] Calculate Candidate x Job Matrix (Multidimensional Array double[][])");

        String sub = prompt("Enter choice (1-5) [Default 1]: ");

        switch (sub.trim()) {
            case "2": {
                double minScore = promptDouble("Enter Minimum Match Score % (e.g. 60.0): ");
                double minExp = promptDouble("Enter Minimum Experience in years (e.g. 2.0): ");
                String reqSkill = prompt("Enter Required Skill filter (optional, press Enter to skip): ");
                String reqEdu = prompt("Enter Education keyword filter (optional, press Enter to skip): ");

                List<MatchResult> filtered = rankingService.filterCandidates(job, minScore, minExp, reqSkill, reqEdu);
                rankingService.displayLeaderboard(job, filtered);
                break;
            }
            case "3": {
                int n = promptInt("Enter Top N count (e.g. 3): ");
                List<MatchResult> topN = rankingService.getTopNCandidates(job, n);
                System.out.printf("\n--- Top %d Highest Scoring Candidates (PriorityQueue Max-Heap) ---\n", n);
                rankingService.displayLeaderboard(job, topN);
                break;
            }
            case "4": {
                TreeMap<Double, List<Candidate>> grouped = rankingService.groupCandidatesByScore(job);
                System.out.println("\n--- Candidate Score Distribution (TreeMap Sorted Descending) ---");
                grouped.forEach((score, list) -> {
                    System.out.printf("Score: %.2f%% -> %d candidate(s): ", score, list.size());
                    list.forEach(c -> System.out.printf("[%s] ", c.getName()));
                    System.out.println();
                });
                break;
            }
            case "5": {
                // Multidimensional array demonstration: double[][] matchingScores
                List<Candidate> allCands = candidateManager.getAllCandidates();
                double[][] matrix = matchingService.calculateMatchingMatrix(allCands, jobs);
                System.out.println("\n--- Candidate x Job Matching Matrix (double[][]) ---");
                System.out.printf("%-20s", "Candidate \\ Job");
                for (Job j : jobs) {
                    System.out.printf(" | %-15s", truncate(j.getJobTitle(), 15));
                }
                System.out.println("\n" + "-".repeat(20 + jobs.size() * 18));
                for (int i = 0; i < allCands.size(); i++) {
                    System.out.printf("%-20s", truncate(allCands.get(i).getName(), 20));
                    for (int j = 0; j < jobs.size(); j++) {
                        System.out.printf(" | %6.2f%%        ", matrix[i][j]);
                    }
                    System.out.println();
                }
                System.out.println("-".repeat(20 + jobs.size() * 18));
                break;
            }
            default: {
                List<MatchResult> ranked = rankingService.rankCandidatesForJob(job);
                rankingService.displayLeaderboard(job, ranked);
                break;
            }
        }
    }

    // ========================================================
    // 11. VIEW MATCHING REPORTS
    // ========================================================
    private static void handleViewMatchingReports()
            throws CandidateNotFoundException, JobNotFoundException, IOException {
        System.out.println("\n--- [11] VIEW MATCHING REPORTS ---");
        List<Candidate> candidates = candidateManager.getAllCandidates();
        List<Job> jobs = jobManager.getAllJobs();

        if (candidates.isEmpty() || jobs.isEmpty()) {
            System.out.println("[!] Candidates and jobs must exist to generate reports.");
            return;
        }

        candidateManager.displayCandidatesTable(candidates);
        int cId = promptInt("\nSelect Candidate ID: ");
        Candidate candidate = candidateManager.searchById(cId);

        jobManager.displayJobsTable(jobs);
        int jId = promptInt("\nSelect Target Job ID: ");
        Job job = jobManager.searchById(jId);

        MatchResult result = matchingService.analyze(candidate, job);
        ReportGenerator.displayReport(result);

        String saveTxt = prompt("Save this report as a TXT file in 'reports/'? (y/n): ");
        if (saveTxt.equalsIgnoreCase("y") || saveTxt.equalsIgnoreCase("yes")) {
            String savedPath = ReportGenerator.saveReportAsTxt(result);
            System.out.printf("[SUCCESS] Report saved to: %s\n", new File(savedPath).getAbsolutePath());
        }
    }

    // ========================================================
    // 12. PROCESS MULTIPLE RESUMES (MULTITHREADING)
    // ========================================================
    private static void handleProcessMultipleResumesMultithreaded() {
        System.out.println("\n--- [12] PROCESS MULTIPLE RESUMES (MULTITHREADED PIPELINE) ---");
        System.out.println("Demonstrates Producer-Consumer coordination with Thread, Runnable, wait(), notifyAll(), and Daemon.");

        String folderOrFiles = prompt("Enter folder path containing resumes (e.g. resumes) or file paths separated by comma: ");
        if (folderOrFiles.isEmpty()) folderOrFiles = "resumes";

        List<File> files = new ArrayList<>();
        String[] parts = folderOrFiles.split(",");

        for (String p : parts) {
            File f = new File(p.trim());
            if (f.exists()) {
                if (f.isDirectory()) {
                    File[] list = f.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".pdf"));
                    if (list != null) {
                        Collections.addAll(files, list);
                    }
                } else if (f.isFile()) {
                    files.add(f);
                }
            }
        }

        if (files.isEmpty()) {
            System.out.println("[!] No resume files found in the specified path: " + folderOrFiles);
            return;
        }

        System.out.printf("Found %d resume file(s) to process concurrently.\n", files.size());

        // Execute multithreaded processing pipeline
        ResumeProcessor processor = new ResumeProcessor(5);
        List<Candidate> extracted = processor.processBatch(files);

        // Refresh Candidate Manager from database
        candidateManager.refreshFromDatabase();

        System.out.println("\nExtracted Candidate Summary:");
        candidateManager.displayCandidatesTable(extracted);
    }

    // ========================================================
    // 13. IMPORT CANDIDATES FROM CSV
    // ========================================================
    private static void handleImportCandidatesFromCsv() throws IOException {
        System.out.println("\n--- [13] IMPORT CANDIDATES FROM CSV ---");
        String path = prompt("Enter CSV file path (e.g. reports/candidates_export.csv): ");
        File file = new File(path.trim());

        if (!file.exists()) {
            System.out.println("[!] File does not exist: " + path);
            return;
        }

        List<Candidate> imported = ReportGenerator.importCandidatesFromCsv(file);
        System.out.printf("\nParsed %d candidate records from CSV. Storing to database...\n", imported.size());

        int count = 0;
        for (Candidate c : imported) {
            try {
                candidateManager.addCandidate(c);
                count++;
            } catch (DuplicateCandidateException e) {
                System.out.printf("  [SKIP] Duplicate: %s (%s)\n", c.getName(), c.getEmail());
            } catch (Exception e) {
                System.out.printf("  [ERROR] %s: %s\n", c.getName(), e.getMessage());
            }
        }

        System.out.printf("\n[SUCCESS] %d candidates imported successfully into system.\n", count);
        candidateManager.refreshFromDatabase();
    }

    // ========================================================
    // 14. EXPORT CANDIDATES AND REPORTS TO CSV
    // ========================================================
    private static void handleExportToCsv() throws IOException {
        System.out.println("\n--- [14] EXPORT CANDIDATES AND REPORTS TO CSV ---");
        System.out.println("Export Options:");
        System.out.println("  [1] Export All Candidates to CSV");
        System.out.println("  [2] Export All Matching Results to CSV");
        System.out.println("  [3] Export Both");

        String opt = prompt("Enter choice (1-3): ");
        List<Candidate> candidates = candidateManager.getAllCandidates();
        List<MatchResult> matches = DatabaseManager.getAllMatchResults();

        switch (opt.trim()) {
            case "1": {
                String f = ReportGenerator.exportCandidatesToCsv(candidates, null);
                System.out.printf("[SUCCESS] Candidates exported to: %s\n", new File(f).getAbsolutePath());
                break;
            }
            case "2": {
                String f = ReportGenerator.exportMatchResultsToCsv(matches, null);
                System.out.printf("[SUCCESS] Matching results exported to: %s\n", new File(f).getAbsolutePath());
                break;
            }
            case "3": {
                String f1 = ReportGenerator.exportCandidatesToCsv(candidates, null);
                String f2 = ReportGenerator.exportMatchResultsToCsv(matches, null);
                System.out.printf("[SUCCESS] Exports created:\n  - %s\n  - %s\n",
                        new File(f1).getAbsolutePath(), new File(f2).getAbsolutePath());
                break;
            }
            default:
                System.out.println("[!] Invalid export choice.");
                break;
        }
    }

    // ========================================================
    // 15. EXIT
    // ========================================================
    private static void handleExit() {
        System.out.println("\nShutting down SmartHire System...");
        DatabaseManager.closeConnection();
        scanner.close();
        System.out.println("Resources released successfully. Thank you for using SmartHire! Goodbye.\n");
    }

    // ========================================================
    // HELPER INPUT PROMPT METHODS
    // ========================================================
    private static String prompt(String msg) {
        System.out.print(msg);
        return scanner.nextLine().trim();
    }

    private static String promptOptional(String msg, String fallback) {
        System.out.print(msg);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? fallback : input;
    }

    private static int promptInt(String msg) {
        while (true) {
            System.out.print(msg);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number. Please enter an integer.");
            }
        }
    }

    private static double promptDouble(String msg) {
        while (true) {
            System.out.print(msg);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid decimal. Please enter a valid number.");
            }
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
