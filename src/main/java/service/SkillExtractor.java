package service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility service to extract technical skills from resume text and job descriptions.
 * Demonstrates:
 * 1. HashSet for identifying unique skills
 * 2. LinkedHashSet for preserving insertion order
 * 3. TreeSet for automatic alphabetical sorting
 * 4. Regular expressions and boundary matching
 */
public class SkillExtractor {

    private static final Map<String, Pattern> SKILL_PATTERNS = new LinkedHashMap<>();

    static {
        // Frameworks & Architecture (Multi-word registered first)
        registerSkill("Spring Boot", "(?i)\\bspring\\s+boot\\b");
        registerSkill("Spring MVC", "(?i)\\bspring\\s+mvc\\b");
        registerSkill("Spring Security", "(?i)\\bspring\\s+security\\b");
        registerSkill("Spring Data", "(?i)\\bspring\\s+data\\b");
        registerSkill("Spring", "(?i)\\bspring\\b(?!\\s+(?:boot|mvc|security|data))");
        registerSkill("REST API", "(?i)\\b(?:restful|rest)\\s+api[s]?\\b|\\brest\\b");
        registerSkill("GraphQL", "(?i)\\bgraphql\\b");
        registerSkill("Microservices", "(?i)\\bmicroservices?\\b|\\bmicro-services?\\b");
        registerSkill("Hibernate", "(?i)\\bhibernate\\b");
        registerSkill("JPA", "(?i)\\bjpa\\b|\\bjava\\s+persistence\\s+api\\b");

        // Languages
        registerSkill("Java", "(?i)\\bjava\\b(?!script)");
        registerSkill("JavaScript", "(?i)\\bjavascript\\b|\\bjs\\b");
        registerSkill("TypeScript", "(?i)\\btypescript\\b|\\bts\\b");
        registerSkill("Python", "(?i)\\bpython\\b");
        registerSkill("C++", "(?i)\\bc\\+\\+\\b");
        registerSkill("C#", "(?i)\\bc#\\b|\\bc\\s*sharp\\b");
        registerSkill("C", "(?i)\\bc\\b(?=[,\\s/&]|$)");
        registerSkill("SQL", "(?i)\\bsql\\b");
        registerSkill("HTML", "(?i)\\bhtml5?\\b");
        registerSkill("CSS", "(?i)\\bcss3?\\b");
        registerSkill("Bash", "(?i)\\bbash\\b|\\bshell\\s+scripting?\\b");

        // Frontend
        registerSkill("React", "(?i)\\breact(\\.js|js)?\\b");
        registerSkill("Angular", "(?i)\\bangular(\\.js|js)?\\b");
        registerSkill("Vue", "(?i)\\bvue(\\.js|js)?\\b");
        registerSkill("Node.js", "(?i)\\bnode(\\.js|js)?\\b");
        registerSkill("Express.js", "(?i)\\bexpress(\\.js|js)?\\b");
        registerSkill("Redux", "(?i)\\bredux\\b");
        registerSkill("Tailwind CSS", "(?i)\\btailwind(\\s+css)?\\b");

        // Databases
        registerSkill("MySQL", "(?i)\\bmysql\\b");
        registerSkill("PostgreSQL", "(?i)\\bpostgres(ql)?\\b");
        registerSkill("SQLite", "(?i)\\bsqlite3?\\b");
        registerSkill("Oracle Database", "(?i)\\boracle(\\s+db|\\s+database)?\\b");
        registerSkill("MongoDB", "(?i)\\bmongodb\\b|\\bmongo\\b");
        registerSkill("Redis", "(?i)\\bredis\\b");

        // Cloud & DevOps
        registerSkill("AWS", "(?i)\\baws\\b|\\bamazon\\s+web\\s+services\\b");
        registerSkill("Azure", "(?i)\\bazure\\b|\\bmicrosoft\\s+azure\\b");
        registerSkill("GCP", "(?i)\\bgcp\\b|\\bgoogle\\s+cloud\\b");
        registerSkill("Docker", "(?i)\\bdocker\\b");
        registerSkill("Kubernetes", "(?i)\\bkubernetes\\b|\\bk8s\\b");
        registerSkill("Jenkins", "(?i)\\bjenkins\\b");
        registerSkill("CI/CD", "(?i)\\bci/cd\\b|\\bci\\s*-\\s*cd\\b");
        registerSkill("Git", "(?i)\\bgit\\b(?!hub|lab)");
        registerSkill("GitHub", "(?i)\\bgithub\\b");
        registerSkill("Linux", "(?i)\\blinux\\b|\\bubuntu\\b");
        registerSkill("Maven", "(?i)\\bmaven\\b");

        // Big Data & AI
        registerSkill("Kafka", "(?i)\\bkafka\\b");
        registerSkill("Spark", "(?i)\\bspark\\b");
        registerSkill("Machine Learning", "(?i)\\bmachine\\s+learning\\b|\\bml\\b");
        registerSkill("Pandas", "(?i)\\bpandas\\b");
        registerSkill("NumPy", "(?i)\\bnumpy\\b");

        // Testing & Practices
        registerSkill("JUnit", "(?i)\\bjunit[0-9]?\\b");
        registerSkill("Mockito", "(?i)\\bmockito\\b");
        registerSkill("OOP", "(?i)\\boop\\b|\\bobject[\\s-]oriented\\b");
        registerSkill("Data Structures", "(?i)\\bdata\\s+structures\\b|\\bdsa\\b");
        registerSkill("Agile", "(?i)\\bagile\\b");
        registerSkill("Scrum", "(?i)\\bscrum\\b");
    }

    private static void registerSkill(String name, String regex) {
        SKILL_PATTERNS.put(name, Pattern.compile(regex));
    }

    /**
     * Extracts skills from text using:
     * - HashSet for identifying unique items
     * - LinkedHashSet for preserving matched order
     * - TreeSet for returning alphabetically sorted skills
     */
    public static List<String> extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // LinkedHashSet to preserve detection order
        Set<String> detectedSkills = new LinkedHashSet<>();

        for (Map.Entry<String, Pattern> entry : SKILL_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(text).find()) {
                detectedSkills.add(entry.getKey());
            }
        }

        // Also check explicit "SKILLS:" label line
        Pattern explicitPattern = Pattern.compile("(?i)(?:skills?|technical skills?|competencies)\\s*[:\\-]([^\r\n]+)");
        Matcher explicitMatcher = explicitPattern.matcher(text);
        if (explicitMatcher.find()) {
            String explicitLine = explicitMatcher.group(1);
            String[] tokens = explicitLine.split("[,;|•]");
            for (String t : tokens) {
                String clean = t.trim();
                if (clean.length() >= 2 && clean.length() <= 30) {
                    String canonical = findCanonical(clean);
                    detectedSkills.add(canonical != null ? canonical : clean);
                }
            }
        }

        // TreeSet to demonstrate automatic sorting
        Set<String> sortedSkills = new TreeSet<>(detectedSkills);
        return new ArrayList<>(sortedSkills);
    }

    /**
     * Parses a comma-separated skills input string.
     */
    public static List<String> extractSkillsFromString(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // HashSet to eliminate duplicates
        Set<String> unique = new HashSet<>();
        String[] tokens = commaSeparated.split(",");
        for (String t : tokens) {
            String clean = t.trim();
            if (!clean.isEmpty()) {
                String canonical = findCanonical(clean);
                unique.add(canonical != null ? canonical : clean);
            }
        }

        // Return sorted list using TreeSet
        return new ArrayList<>(new TreeSet<>(unique));
    }

    public static String findCanonical(String token) {
        if (token == null) return null;
        for (Map.Entry<String, Pattern> entry : SKILL_PATTERNS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(token.trim())) {
                return entry.getKey();
            }
            if (entry.getValue().matcher(token.trim()).matches()) {
                return entry.getKey();
            }
        }
        return null;
    }
}
