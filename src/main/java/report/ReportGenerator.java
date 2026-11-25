package report;

import database.DatabaseManager;
import model.Candidate;
import model.Job;
import model.MatchResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates candidate-job evaluation reports and handles CSV import/export.
 * Demonstrates:
 * 1. File I/O: FileInputStream, FileOutputStream, FileReader, FileWriter,
 *    BufferedReader, BufferedWriter
 * 2. StringBuilder for rich text formatting
 * 3. Structured CSV parsing, validation, and generation
 * 4. Error and exception handling with try-with-resources
 */
public class ReportGenerator {

    private static final String REPORTS_DIR = "reports";
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        File dir = new File(REPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     * Prints a detailed candidate evaluation report to the console using StringBuilder.
     */
    public static void displayReport(MatchResult result) {
        if (result == null) {
            System.out.println("[!] No match result to display.");
            return;
        }

        StringBuilder sb = buildReportStringBuilder(result);
        System.out.println(sb.toString());
    }

    /**
     * Saves the detailed candidate analysis report as a TXT file using FileWriter and BufferedWriter.
     */
    public static String saveReportAsTxt(MatchResult result) throws IOException {
        Candidate c = result.getCandidate();
        String safeName = c.getName().replaceAll("[^A-Za-z0-9_-]", "_");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = REPORTS_DIR + "/report_" + safeName + "_" + ts + ".txt";

        StringBuilder sb = buildReportStringBuilder(result);

        // Demonstrates FileWriter and BufferedWriter
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))) {
            bw.write(sb.toString());
        }

        return filename;
    }

    /**
     * Builds the complete structured report string using StringBuilder.
     */
    private static StringBuilder buildReportStringBuilder(MatchResult result) {
        Candidate c = result.getCandidate();
        Job j = result.getJob();

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(85)).append("\n");
        sb.append("                   SMARTHIRE CANDIDATE EVALUATION REPORT\n");
        sb.append("=".repeat(85)).append("\n");
        sb.append("Report Date   : ").append(LocalDateTime.now().format(TS_FORMAT)).append("\n");
        sb.append("-".repeat(85)).append("\n");

        sb.append("CANDIDATE INFORMATION:\n");
        sb.append("  Candidate ID: #").append(c.getCandidateId()).append("\n");
        sb.append("  Name        : ").append(c.getName()).append("\n");
        sb.append("  Email       : ").append(c.getEmail()).append("\n");
        sb.append("  Phone       : ").append(c.getPhone().isEmpty() ? "N/A" : c.getPhone()).append("\n");
        sb.append("  Education   : ").append(c.getEducation()).append("\n");
        sb.append("  Experience  : ").append(c.getExperienceYears()).append(" years\n");
        sb.append("  Skills      : ").append(c.getSkillsCommaSeparated()).append("\n");

        sb.append("\nJOB INFORMATION:\n");
        sb.append("  Job ID      : #").append(j.getJobId()).append("\n");
        sb.append("  Job Title   : ").append(j.getJobTitle()).append("\n");
        sb.append("  Company     : ").append(j.getCompanyName()).append("\n");
        sb.append("  Req Skills  : ").append(j.getRequiredSkillsCommaSeparated()).append("\n");
        sb.append("  Pref Skills : ").append(j.getPreferredSkillsCommaSeparated()).append("\n");
        sb.append("  Min Exp     : ").append(j.getMinimumExperience()).append(" years\n");

        sb.append("\n").append("-".repeat(85)).append("\n");
        sb.append("ANALYSIS & SCORE BREAKDOWN:\n");
        sb.append(String.format("  1. Technical Skills Score (Weight 40%%) : %5.1f / 40.0 pts  [%s]\n",
                result.getSkillsScore(), renderBar(result.getSkillsScore(), 40.0)));
        sb.append(String.format("  2. Work Experience Score  (Weight 25%%) : %5.1f / 25.0 pts  [%s]\n",
                result.getExperienceScore(), renderBar(result.getExperienceScore(), 25.0)));
        sb.append(String.format("  3. Education Score        (Weight 20%%) : %5.1f / 20.0 pts  [%s]\n",
                result.getEducationScore(), renderBar(result.getEducationScore(), 20.0)));
        sb.append(String.format("  4. Projects & Certs Score (Weight 15%%) : %5.1f / 15.0 pts  [%s]\n",
                result.getProjectsScore(), renderBar(result.getProjectsScore(), 15.0)));
        sb.append("  ").append(".".repeat(65)).append("\n");
        sb.append(String.format("  FINAL MATCHING PERCENTAGE : %6.2f%% (%s)\n",
                result.getFinalScore(), result.getMatchTier()));

        sb.append("\nSKILLS ALIGNMENT:\n");
        sb.append("  Matched Skills (").append(result.getMatchedSkills().size()).append("): ")
          .append(result.getMatchedSkills().isEmpty() ? "None" : String.join(", ", result.getMatchedSkills())).append("\n");
        sb.append("  Missing Skills (").append(result.getMissingSkills().size()).append("): ")
          .append(result.getMissingSkills().isEmpty() ? "None (Fully Qualified)" : String.join(", ", result.getMissingSkills())).append("\n");

        sb.append("\nRECOMMENDATIONS & ACTION ITEMS:\n");
        if (result.getRecommendations().isEmpty()) {
            sb.append("  Candidate fully satisfies job criteria.\n");
        } else {
            int idx = 1;
            for (String r : result.getRecommendations()) {
                sb.append("  ").append(idx++).append(". ").append(r).append("\n");
            }
        }
        sb.append("=".repeat(85)).append("\n");
        return sb;
    }

    /**
     * Exports all Candidates to a CSV file using FileOutputStream and OutputStreamWriter.
     */
    public static String exportCandidatesToCsv(List<Candidate> candidates, String customFilename) throws IOException {
        String filename = customFilename != null && !customFilename.isEmpty() ?
                customFilename : REPORTS_DIR + "/candidates_export.csv";

        try (FileOutputStream fos = new FileOutputStream(filename);
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {

            // CSV Header
            bw.write("Candidate ID,Name,Email,Phone,Education,Experience,Skills,Projects,Certifications,Resume Path");
            bw.newLine();

            for (Candidate c : candidates) {
                bw.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.1f,\"%s\",\"%s\",\"%s\",\"%s\"",
                        c.getCandidateId(),
                        escapeCsv(c.getName()),
                        escapeCsv(c.getEmail()),
                        escapeCsv(c.getPhone()),
                        escapeCsv(c.getEducation()),
                        c.getExperienceYears(),
                        escapeCsv(c.getSkillsCommaSeparated()),
                        escapeCsv(c.getProjectsJoined()),
                        escapeCsv(c.getCertificationsJoined()),
                        escapeCsv(c.getResumePath())
                ));
                bw.newLine();
            }
        }
        return filename;
    }

    /**
     * Exports Match Results to a CSV file.
     */
    public static String exportMatchResultsToCsv(List<MatchResult> matchResults, String customFilename) throws IOException {
        String filename = customFilename != null && !customFilename.isEmpty() ?
                customFilename : REPORTS_DIR + "/matching_results_export.csv";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))) {
            bw.write("Candidate ID,Candidate Name,Email,Job ID,Job Title,Company,Final Score %,Skills Score,Exp Score,Edu Score,Proj Score,Tier,Missing Skills");
            bw.newLine();

            for (MatchResult mr : matchResults) {
                Candidate c = mr.getCandidate();
                Job j = mr.getJob();
                bw.write(String.format("%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,\"%s\",\"%s\"",
                        c.getCandidateId(),
                        escapeCsv(c.getName()),
                        escapeCsv(c.getEmail()),
                        j.getJobId(),
                        escapeCsv(j.getJobTitle()),
                        escapeCsv(j.getCompanyName()),
                        mr.getFinalScore(),
                        mr.getSkillsScore(),
                        mr.getExperienceScore(),
                        mr.getEducationScore(),
                        mr.getProjectsScore(),
                        escapeCsv(mr.getMatchTier()),
                        escapeCsv(String.join("; ", mr.getMissingSkills()))
                ));
                bw.newLine();
            }
        }
        return filename;
    }

    /**
     * Imports Candidate records from a CSV file.
     * Demonstrates FileReader, BufferedReader, String.split(), and data validation.
     */
    public static List<Candidate> importCandidatesFromCsv(File csvFile) throws IOException {
        if (!csvFile.exists()) {
            throw new FileNotFoundException("CSV file does not exist: " + csvFile.getAbsolutePath());
        }

        List<Candidate> importedList = new ArrayList<>();
        int lineNum = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                // Skip header line
                if (lineNum == 1 && line.toLowerCase().contains("email")) {
                    continue;
                }

                // Parse CSV columns
                String[] cols = parseCsvLine(line);
                if (cols.length >= 6) {
                    try {
                        String name = cols[1].trim();
                        String email = cols[2].trim().toLowerCase();
                        String phone = cols.length > 3 ? cols[3].trim() : "";
                        String education = cols.length > 4 ? cols[4].trim() : "Bachelor's";
                        double exp = Double.parseDouble(cols[5].trim());

                        List<String> skills = (cols.length > 6 && !cols[6].trim().isEmpty()) ?
                                Arrays.asList(cols[6].split("[,;]")) : new ArrayList<>();
                        List<String> projects = (cols.length > 7 && !cols[7].trim().isEmpty()) ?
                                Arrays.asList(cols[7].split("\\|")) : new ArrayList<>();
                        List<String> certs = (cols.length > 8 && !cols[8].trim().isEmpty()) ?
                                Arrays.asList(cols[8].split("\\|")) : new ArrayList<>();
                        String resumePath = cols.length > 9 ? cols[9].trim() : csvFile.getName();

                        Candidate c = new Candidate(name, email, phone, education, exp, skills, projects, certs, resumePath);
                        importedList.add(c);
                    } catch (Exception e) {
                        System.err.printf("[CSV Import Warning] Skipping malformed line %d: %s\n", lineNum, e.getMessage());
                    }
                }
            }
        }
        return importedList;
    }

    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }

    private static String renderBar(double val, double max) {
        int length = 20;
        int filled = (int) Math.round((val / max) * length);
        filled = Math.max(0, Math.min(length, filled));
        return "#".repeat(filled) + "-".repeat(length - filled);
    }
}
