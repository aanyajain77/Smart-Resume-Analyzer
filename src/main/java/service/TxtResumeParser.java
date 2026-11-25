package service;

import exceptions.InvalidResumeException;
import model.Candidate;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Concrete implementation of ResumeParser interface.
 * Demonstrates:
 * 1. Interface implementation and polymorphism
 * 2. File I/O: FileReader, BufferedReader, FileInputStream, StreamTokenizer
 * 3. String methods: contains(), split(), equalsIgnoreCase(), trim(), replace(), toLowerCase()
 * 4. Wrapper classes: Integer, Double
 * 5. StringBuilder for text buffering
 * 6. Dynamic reflection for optional PDFBox support without strict dependency
 */
public class TxtResumeParser implements ResumeParser {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "(?i)\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+?\\d{1,3}[-.\s]?)?\\(?\\d{3}\\)?[-.\s]?\\d{3}[-.\s]?\\d{4}|\\b[6-9]\\d{9}\\b"
    );

    private static final Pattern NAME_LABEL_PATTERN = Pattern.compile(
        "(?i)^(?:candidate\\s+)?name\\s*[:\\-]\\s*([A-Za-z\\s.'-]+)$", Pattern.MULTILINE
    );

    private static final Pattern DEGREE_PATTERN = Pattern.compile(
        "(?i)\\b(Ph\\.?D|Doctorate|M\\.?S\\.?|Master(?:'s)?(?:\\s+of\\s+[A-Za-z\\s]+)?|M\\.?Tech|MBA|MCA|" +
        "B\\.?S\\.?|Bachelor(?:'s)?(?:\\s+of\\s+[A-Za-z\\s]+)?|B\\.?Tech|B\\.?E\\.?|BCA|Diploma|Associate)\\b([^\\r\\n]*)"
    );

    private static final Pattern EXP_PATTERN = Pattern.compile(
        "(?i)(\\d+(?:\\.\\d+)?)\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+(?:work\\s+|professional\\s+|hands-on\\s+)?experience"
    );

    private static final Pattern YEAR_RANGE_PATTERN = Pattern.compile(
        "\\b(20\\d{2}|19\\d{2})\\s*(?:-|–|to)\\s*(20\\d{2}|Present|Current|Now)\\b", Pattern.CASE_INSENSITIVE
    );

    @Override
    public Candidate parse(File file) throws InvalidResumeException, IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new InvalidResumeException("Resume file not found or invalid: " + (file != null ? file.getPath() : "null"));
        }

        String rawContent = extractRawText(file);
        if (rawContent == null || rawContent.trim().isEmpty()) {
            throw new InvalidResumeException("The resume file is empty or unreadable: " + file.getName());
        }

        Candidate candidate = new Candidate();
        candidate.setResumePath(file.getAbsolutePath());

        // Extract Email
        String email = extractEmail(rawContent);
        if (email.isEmpty()) {
            email = "candidate_" + System.currentTimeMillis() + "@smarthire.local";
        }
        candidate.setEmail(email);

        // Extract Phone
        candidate.setPhone(extractPhone(rawContent));

        // Extract Name
        candidate.setName(extractName(rawContent, file.getName()));

        // Extract Skills using SkillExtractor
        List<String> skills = SkillExtractor.extractSkills(rawContent);
        candidate.setSkills(skills);

        // Extract Education
        candidate.setEducation(extractEducation(rawContent));

        // Extract Experience
        candidate.setExperienceYears(extractExperience(rawContent));

        // Extract Projects
        candidate.setProjects(extractSectionItems(rawContent, "PROJECTS", "ACADEMIC PROJECTS", "KEY PROJECTS"));

        // Extract Certifications
        candidate.setCertifications(extractCertifications(rawContent));

        return candidate;
    }

    @Override
    public String extractRawText(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".txt") || name.endsWith(".csv")) {
            return readTextUsingBufferedReader(file);
        } else if (name.endsWith(".pdf")) {
            return readPdfWithPdfBox(file);
        }
        return readTextUsingBufferedReader(file);
    }

    /**
     * Reads text using FileReader, BufferedReader, and StringBuilder.
     * Also demonstrates StreamTokenizer usage.
     */
    private String readTextUsingBufferedReader(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (Exception e) {
            // Fallback reading using FileInputStream
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = fis.readAllBytes();
                return new String(data, StandardCharsets.ISO_8859_1);
            }
        }

        // Demonstrate StreamTokenizer usage
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8)) {
            StreamTokenizer tokenizer = new StreamTokenizer(fr);
            tokenizer.wordChars('_', '_');
            int token = tokenizer.nextToken();
            // Verify tokenizer can inspect first token
        } catch (Exception ignored) {}

        return sb.toString();
    }

    /**
     * Optional PDFBox reading using reflection.
     */
    private String readPdfWithPdfBox(File file) throws IOException {
        try {
            Class<?> pdDocClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
            Method loadMethod = pdDocClass.getMethod("load", File.class);
            Object document = loadMethod.invoke(null, file);

            Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
            Object stripper = stripperClass.getDeclaredConstructor().newInstance();

            Method getTextMethod = stripperClass.getMethod("getText", pdDocClass);
            String text = (String) getTextMethod.invoke(stripper, document);

            Method closeMethod = pdDocClass.getMethod("close");
            closeMethod.invoke(document);

            return text;
        } catch (ClassNotFoundException e) {
            throw new IOException("Apache PDFBox library not available to read PDF. Please use TXT resumes or add PDFBox dependency.", e);
        } catch (Exception e) {
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim().toLowerCase();
        }
        return "";
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return "";
    }

    private String extractName(String text, String filename) {
        // Look for explicit label "Name: Alice Johnson"
        Matcher labelMatcher = NAME_LABEL_PATTERN.matcher(text);
        if (labelMatcher.find()) {
            String candidateName = labelMatcher.group(1).trim();
            if (candidateName.length() >= 2 && candidateName.length() <= 50) {
                return candidateName;
            }
        }

        // Scan first 5 lines
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < Math.min(lines.length, 6); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String lower = line.toLowerCase();
            if (lower.startsWith("resume") || lower.startsWith("curriculum") || lower.contains("@")
                || PHONE_PATTERN.matcher(line).find() || lower.startsWith("profile")) {
                continue;
            }
            if (line.matches("^[A-Za-z][A-Za-z\\s.'-]{1,45}$")) {
                return line;
            }
        }

        // Fallback: derive from filename (e.g. resume1_alice.txt -> Alice)
        String cleanName = filename.replaceFirst("[.][^.]+$", "")
                .replaceAll("(?i)^(resume[0-9]*[_\\-\\s]*|cv[_\\-\\s]*)", "")
                .replace("_", " ")
                .replace("-", " ")
                .trim();

        if (!cleanName.isEmpty()) {
            String[] parts = cleanName.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty()) {
                    sb.append(Character.toUpperCase(p.charAt(0)))
                      .append(p.substring(1).toLowerCase())
                      .append(" ");
                }
            }
            return sb.toString().trim();
        }

        return "Candidate";
    }

    private String extractEducation(String text) {
        Matcher matcher = DEGREE_PATTERN.matcher(text);
        if (matcher.find()) {
            String deg = matcher.group(1).trim();
            String rest = matcher.group(2).trim().replaceAll("[,;\\|]+$", "").trim();
            return rest.isEmpty() ? deg : deg + " " + rest;
        }

        String lower = text.toLowerCase();
        if (lower.contains("b.tech") || lower.contains("btech")) return "B.Tech in Computer Science/IT";
        if (lower.contains("bachelor")) return "Bachelor's Degree";
        if (lower.contains("master") || lower.contains("m.s")) return "Master's Degree";
        if (lower.contains("ph.d") || lower.contains("phd")) return "Ph.D.";
        if (lower.contains("bca")) return "BCA in Computer Applications";
        if (lower.contains("mca")) return "MCA in Computer Applications";

        return "Bachelor's Degree";
    }

    private double extractExperience(String text) {
        // Pattern 1: explicit "X years of experience"
        Matcher matcher = EXP_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                // Wrapper class Double parsing
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // Pattern 2: Year intervals
        Matcher yearMatcher = YEAR_RANGE_PATTERN.matcher(text);
        int currentYear = Year.now().getValue();
        double totalYears = 0.0;
        int count = 0;

        while (yearMatcher.find()) {
            try {
                // Wrapper class Integer parsing
                int start = Integer.parseInt(yearMatcher.group(1));
                String endStr = yearMatcher.group(2);
                int end = (endStr.equalsIgnoreCase("present") ||
                           endStr.equalsIgnoreCase("current") ||
                           endStr.equalsIgnoreCase("now")) ? currentYear : Integer.parseInt(endStr);

                if (end >= start && (end - start) <= 40) {
                    totalYears += (end - start);
                    count++;
                }
            } catch (Exception ignored) {}
        }

        if (count > 0) {
            return Math.min(totalYears, 25.0);
        }

        return 0.0;
    }

    private List<String> extractSectionItems(String text, String... headers) {
        List<String> list = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        boolean inSection = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            boolean matchHeader = false;
            for (String h : headers) {
                if (trimmed.toUpperCase().matches("^(?:#+\\s*)?(?:" + Pattern.quote(h) + ")[:\\s-]*$")) {
                    matchHeader = true;
                    break;
                }
            }

            if (matchHeader) {
                inSection = true;
                continue;
            }

            if (inSection && trimmed.toUpperCase().matches("^(?:#+\\s*)?[A-Z\\s]{3,25}[:\\s-]*$") && !matchHeader) {
                inSection = false;
            }

            if (inSection) {
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•") ||
                    trimmed.matches("^\\d+\\.\\s+.*")) {
                    String clean = trimmed.replaceAll("^[-*•\\d.]+\\s*", "").trim();
                    if (clean.length() >= 4 && clean.length() <= 120) {
                        list.add(clean);
                    }
                } else if (trimmed.length() >= 4 && trimmed.length() <= 80 && !trimmed.endsWith(".")) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }

    private List<String> extractCertifications(String text) {
        List<String> certs = new ArrayList<>();
        Pattern[] patterns = {
            Pattern.compile("(?i)AWS Certified [A-Za-z\\s-]+"),
            Pattern.compile("(?i)Oracle Certified [A-Za-z\\s0-9:]+"),
            Pattern.compile("(?i)Microsoft Certified [A-Za-z\\s]+"),
            Pattern.compile("(?i)Google Cloud Certified [A-Za-z\\s]+"),
            Pattern.compile("(?i)Certified Kubernetes [A-Za-z\\s]+"),
            Pattern.compile("(?i)PMP|Project Management Professional"),
            Pattern.compile("(?i)Certified Scrum Master|CSM"),
            Pattern.compile("(?i)Coursera [A-Za-z\\s]+")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            while (m.find()) {
                String match = m.group().trim();
                if (!certs.contains(match)) {
                    certs.add(match);
                }
            }
        }

        List<String> section = extractSectionItems(text, "CERTIFICATIONS", "CERTIFICATES");
        for (String c : section) {
            if (!certs.contains(c)) {
                certs.add(c);
            }
        }

        return certs;
    }
}
