package database;

import exceptions.CandidateNotFoundException;
import exceptions.DuplicateCandidateException;
import exceptions.JobNotFoundException;
import model.Candidate;
import model.Job;
import model.MatchResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages SQLite database connectivity and executes CRUD operations using JDBC.
 * Demonstrates:
 * 1. DriverManager and Connection
 * 2. PreparedStatement for parameterized queries
 * 3. Statement for DDL execution
 * 4. executeQuery(), executeUpdate(), ResultSet
 * 5. try-with-resources for safe resource cleanup
 * 6. Permanent storage in database/smarthire.db
 */
public class DatabaseManager {
    private static final String DB_DIR = "database";
    private static final String DB_FILE = DB_DIR + "/smarthire.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection = null;
    private static boolean sqliteDriverLoaded = false;

    // In-memory / file fallback for standalone execution if SQLite driver jar is missing
    private static final Map<Integer, Candidate> fallbackCandidates = new LinkedHashMap<>();
    private static final Map<Integer, Job> fallbackJobs = new LinkedHashMap<>();
    private static final List<MatchResult> fallbackMatches = new ArrayList<>();
    private static final AtomicInteger candSeq = new AtomicInteger(1);
    private static final AtomicInteger jobSeq = new AtomicInteger(1);
    private static final AtomicInteger matchSeq = new AtomicInteger(1);

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            sqliteDriverLoaded = true;
        } catch (ClassNotFoundException e) {
            sqliteDriverLoaded = false;
        }
    }

    /**
     * Obtains an active connection to the SQLite database.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (!sqliteDriverLoaded) {
            throw new SQLException("SQLite JDBC Driver not found on classpath.");
        }
        if (connection == null || connection.isClosed()) {
            File dir = new File(DB_DIR);
            if (!dir.exists()) dir.mkdirs();

            connection = DriverManager.getConnection(DB_URL);
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return connection;
    }

    /**
     * Initializes all SQLite tables: Candidates, Jobs, MatchResults.
     */
    public static synchronized void initializeDatabase() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) dir.mkdirs();

        if (!sqliteDriverLoaded) {
            System.out.println("[Database] Standalone Mode: SQLite driver not on classpath. Using local persistent storage in 'database/'.");
            loadFallbackData();
            return;
        }

        String createCandidatesTable = """
            CREATE TABLE IF NOT EXISTS Candidates (
                candidate_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                phone TEXT,
                education TEXT,
                experience REAL DEFAULT 0,
                skills TEXT,
                projects TEXT,
                certifications TEXT,
                resume_path TEXT
            );
        """;

        String createJobsTable = """
            CREATE TABLE IF NOT EXISTS Jobs (
                job_id INTEGER PRIMARY KEY AUTOINCREMENT,
                job_title TEXT NOT NULL,
                company_name TEXT NOT NULL,
                required_skills TEXT NOT NULL,
                preferred_skills TEXT,
                minimum_experience REAL DEFAULT 0,
                required_education TEXT
            );
        """;

        String createMatchResultsTable = """
            CREATE TABLE IF NOT EXISTS MatchResults (
                match_id INTEGER PRIMARY KEY AUTOINCREMENT,
                candidate_id INTEGER NOT NULL,
                job_id INTEGER NOT NULL,
                skills_score REAL,
                experience_score REAL,
                education_score REAL,
                projects_score REAL,
                final_score REAL,
                missing_skills TEXT,
                FOREIGN KEY (candidate_id) REFERENCES Candidates(candidate_id) ON DELETE CASCADE,
                FOREIGN KEY (job_id) REFERENCES Jobs(job_id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createCandidatesTable);
            stmt.execute(createJobsTable);
            stmt.execute(createMatchResultsTable);
            System.out.println("[Database] Connected successfully to SQLite database: " + DB_FILE);
        } catch (SQLException e) {
            System.err.println("[Database Error] Schema creation failed: " + e.getMessage() + ". Switching to local persistent storage.");
            sqliteDriverLoaded = false;
            loadFallbackData();
        }
    }

    // ==========================================
    // CANDIDATE CRUD OPERATIONS
    // ==========================================

    public static boolean candidateExistsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String normalizedEmail = email.trim().toLowerCase();

        if (sqliteDriverLoaded) {
            String sql = "SELECT COUNT(*) FROM Candidates WHERE LOWER(email) = LOWER(?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, normalizedEmail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DB Error] candidateExistsByEmail: " + e.getMessage());
            }
        }

        // Fallback check
        for (Candidate c : fallbackCandidates.values()) {
            if (c.getEmail().equalsIgnoreCase(normalizedEmail)) {
                return true;
            }
        }
        return false;
    }

    public static int insertCandidate(Candidate candidate) throws DuplicateCandidateException, SQLException {
        if (candidateExistsByEmail(candidate.getEmail())) {
            throw new DuplicateCandidateException("Candidate with email '" + candidate.getEmail() + "' is already registered.");
        }

        if (sqliteDriverLoaded) {
            String sql = """
                INSERT INTO Candidates (name, email, phone, education, experience, 
                                        skills, projects, certifications, resume_path)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, candidate.getName());
                ps.setString(2, candidate.getEmail());
                ps.setString(3, candidate.getPhone());
                ps.setString(4, candidate.getEducation());
                ps.setDouble(5, candidate.getExperienceYears());
                ps.setString(6, candidate.getSkillsCommaSeparated());
                ps.setString(7, candidate.getProjectsJoined());
                ps.setString(8, candidate.getCertificationsJoined());
                ps.setString(9, candidate.getResumePath());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int generatedId = rs.getInt(1);
                            candidate.setCandidateId(generatedId);
                            return generatedId;
                        }
                    }
                }
            }
        }

        // Fallback mode
        int id = candSeq.getAndIncrement();
        candidate.setCandidateId(id);
        fallbackCandidates.put(id, candidate);
        saveFallbackData();
        return id;
    }

    public static List<Candidate> getAllCandidates() {
        if (sqliteDriverLoaded) {
            List<Candidate> list = new ArrayList<>();
            String sql = "SELECT * FROM Candidates ORDER BY candidate_id ASC";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToCandidate(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[DB Error] getAllCandidates: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackCandidates.values());
    }

    public static Candidate getCandidateById(int candidateId) throws CandidateNotFoundException {
        if (sqliteDriverLoaded) {
            String sql = "SELECT * FROM Candidates WHERE candidate_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToCandidate(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DB Error] getCandidateById: " + e.getMessage());
            }
        }

        Candidate c = fallbackCandidates.get(candidateId);
        if (c != null) return c;
        throw new CandidateNotFoundException("Candidate with ID #" + candidateId + " not found.");
    }

    public static boolean updateCandidate(Candidate candidate) {
        if (sqliteDriverLoaded) {
            String sql = """
                UPDATE Candidates
                SET name = ?, email = ?, phone = ?, education = ?, experience = ?,
                    skills = ?, projects = ?, certifications = ?, resume_path = ?
                WHERE candidate_id = ?;
            """;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, candidate.getName());
                ps.setString(2, candidate.getEmail());
                ps.setString(3, candidate.getPhone());
                ps.setString(4, candidate.getEducation());
                ps.setDouble(5, candidate.getExperienceYears());
                ps.setString(6, candidate.getSkillsCommaSeparated());
                ps.setString(7, candidate.getProjectsJoined());
                ps.setString(8, candidate.getCertificationsJoined());
                ps.setString(9, candidate.getResumePath());
                ps.setInt(10, candidate.getCandidateId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[DB Error] updateCandidate: " + e.getMessage());
                return false;
            }
        }

        if (fallbackCandidates.containsKey(candidate.getCandidateId())) {
            fallbackCandidates.put(candidate.getCandidateId(), candidate);
            saveFallbackData();
            return true;
        }
        return false;
    }

    public static boolean deleteCandidate(int candidateId) throws CandidateNotFoundException {
        if (sqliteDriverLoaded) {
            String sql = "DELETE FROM Candidates WHERE candidate_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                int rows = ps.executeUpdate();
                if (rows > 0) return true;
            } catch (SQLException e) {
                System.err.println("[DB Error] deleteCandidate: " + e.getMessage());
                return false;
            }
        }

        if (fallbackCandidates.remove(candidateId) != null) {
            saveFallbackData();
            return true;
        }
        throw new CandidateNotFoundException("Cannot delete: Candidate with ID #" + candidateId + " does not exist.");
    }

    private static Candidate mapResultSetToCandidate(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId(rs.getInt("candidate_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setEducation(rs.getString("education"));
        c.setExperienceYears(rs.getDouble("experience"));
        c.setSkillsFromCommaSeparated(rs.getString("skills"));
        c.setProjectsFromJoined(rs.getString("projects"));
        c.setCertificationsFromJoined(rs.getString("certifications"));
        c.setResumePath(rs.getString("resume_path"));
        return c;
    }

    // ==========================================
    // JOB CRUD OPERATIONS
    // ==========================================

    public static int insertJob(Job job) throws SQLException {
        if (sqliteDriverLoaded) {
            String sql = """
                INSERT INTO Jobs (job_title, company_name, required_skills, preferred_skills, 
                                  minimum_experience, required_education)
                VALUES (?, ?, ?, ?, ?, ?);
            """;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, job.getJobTitle());
                ps.setString(2, job.getCompanyName());
                ps.setString(3, job.getRequiredSkillsCommaSeparated());
                ps.setString(4, job.getPreferredSkillsCommaSeparated());
                ps.setDouble(5, job.getMinimumExperience());
                ps.setString(6, job.getRequiredEducation());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int generatedId = rs.getInt(1);
                            job.setJobId(generatedId);
                            return generatedId;
                        }
                    }
                }
            }
        }

        // Fallback mode
        int id = jobSeq.getAndIncrement();
        job.setJobId(id);
        fallbackJobs.put(id, job);
        saveFallbackData();
        return id;
    }

    public static List<Job> getAllJobs() {
        if (sqliteDriverLoaded) {
            List<Job> list = new ArrayList<>();
            String sql = "SELECT * FROM Jobs ORDER BY job_id ASC";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToJob(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[DB Error] getAllJobs: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackJobs.values());
    }

    public static Job getJobById(int jobId) throws JobNotFoundException {
        if (sqliteDriverLoaded) {
            String sql = "SELECT * FROM Jobs WHERE job_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, jobId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToJob(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DB Error] getJobById: " + e.getMessage());
            }
        }

        Job j = fallbackJobs.get(jobId);
        if (j != null) return j;
        throw new JobNotFoundException("Job with ID #" + jobId + " not found.");
    }

    public static boolean updateJob(Job job) {
        if (sqliteDriverLoaded) {
            String sql = """
                UPDATE Jobs
                SET job_title = ?, company_name = ?, required_skills = ?, preferred_skills = ?,
                    minimum_experience = ?, required_education = ?
                WHERE job_id = ?;
            """;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, job.getJobTitle());
                ps.setString(2, job.getCompanyName());
                ps.setString(3, job.getRequiredSkillsCommaSeparated());
                ps.setString(4, job.getPreferredSkillsCommaSeparated());
                ps.setDouble(5, job.getMinimumExperience());
                ps.setString(6, job.getRequiredEducation());
                ps.setInt(7, job.getJobId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[DB Error] updateJob: " + e.getMessage());
                return false;
            }
        }

        if (fallbackJobs.containsKey(job.getJobId())) {
            fallbackJobs.put(job.getJobId(), job);
            saveFallbackData();
            return true;
        }
        return false;
    }

    public static boolean deleteJob(int jobId) throws JobNotFoundException {
        if (sqliteDriverLoaded) {
            String sql = "DELETE FROM Jobs WHERE job_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, jobId);
                int rows = ps.executeUpdate();
                if (rows > 0) return true;
            } catch (SQLException e) {
                System.err.println("[DB Error] deleteJob: " + e.getMessage());
                return false;
            }
        }

        if (fallbackJobs.remove(jobId) != null) {
            saveFallbackData();
            return true;
        }
        throw new JobNotFoundException("Cannot delete: Job with ID #" + jobId + " does not exist.");
    }

    private static Job mapResultSetToJob(ResultSet rs) throws SQLException {
        Job j = new Job();
        j.setJobId(rs.getInt("job_id"));
        j.setJobTitle(rs.getString("job_title"));
        j.setCompanyName(rs.getString("company_name"));
        j.setRequiredSkillsFromCommaSeparated(rs.getString("required_skills"));
        j.setPreferredSkillsFromCommaSeparated(rs.getString("preferred_skills"));
        j.setMinimumExperience(rs.getDouble("minimum_experience"));
        j.setRequiredEducation(rs.getString("required_education"));
        return j;
    }

    // ==========================================
    // MATCH RESULTS CRUD OPERATIONS
    // ==========================================

    public static int insertMatchResult(MatchResult mr) {
        if (sqliteDriverLoaded) {
            String sql = """
                INSERT INTO MatchResults (candidate_id, job_id, skills_score, experience_score,
                                          education_score, projects_score, final_score, missing_skills)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, mr.getCandidate().getCandidateId());
                ps.setInt(2, mr.getJob().getJobId());
                ps.setDouble(3, mr.getSkillsScore());
                ps.setDouble(4, mr.getExperienceScore());
                ps.setDouble(5, mr.getEducationScore());
                ps.setDouble(6, mr.getProjectsScore());
                ps.setDouble(7, mr.getFinalScore());
                ps.setString(8, String.join(", ", mr.getMissingSkills()));

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int genId = rs.getInt(1);
                            mr.setMatchId(genId);
                            return genId;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DB Error] insertMatchResult: " + e.getMessage());
            }
        }

        int id = matchSeq.getAndIncrement();
        mr.setMatchId(id);
        fallbackMatches.add(mr);
        return id;
    }

    public static List<MatchResult> getAllMatchResults() {
        if (sqliteDriverLoaded) {
            List<MatchResult> list = new ArrayList<>();
            String sql = "SELECT * FROM MatchResults ORDER BY match_id ASC";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int cId = rs.getInt("candidate_id");
                    int jId = rs.getInt("job_id");
                    try {
                        Candidate c = getCandidateById(cId);
                        Job j = getJobById(jId);
                        MatchResult mr = new MatchResult();
                        mr.setMatchId(rs.getInt("match_id"));
                        mr.setCandidate(c);
                        mr.setJob(j);
                        mr.setSkillsScore(rs.getDouble("skills_score"));
                        mr.setExperienceScore(rs.getDouble("experience_score"));
                        mr.setEducationScore(rs.getDouble("education_score"));
                        mr.setProjectsScore(rs.getDouble("projects_score"));
                        mr.setFinalScore(rs.getDouble("final_score"));
                        list.add(mr);
                    } catch (Exception ignored) {}
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[DB Error] getAllMatchResults: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackMatches);
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("[DB Error] Closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    // ==========================================
    // PERSISTENT FALLBACK FILE STORE (database/)
    // ==========================================
    private static synchronized void saveFallbackData() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) dir.mkdirs();

            // Save Candidates
            File candFile = new File(dir, "candidates_data.txt");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(candFile), StandardCharsets.UTF_8))) {
                for (Candidate c : fallbackCandidates.values()) {
                    pw.printf("%d\t%s\t%s\t%s\t%s\t%.1f\t%s\t%s\t%s\t%s\n",
                            c.getCandidateId(),
                            clean(c.getName()),
                            clean(c.getEmail()),
                            clean(c.getPhone()),
                            clean(c.getEducation()),
                            c.getExperienceYears(),
                            clean(c.getSkillsCommaSeparated()),
                            clean(c.getProjectsJoined()),
                            clean(c.getCertificationsJoined()),
                            clean(c.getResumePath()));
                }
            }

            // Save Jobs
            File jobsFile = new File(dir, "jobs_data.txt");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jobsFile), StandardCharsets.UTF_8))) {
                for (Job j : fallbackJobs.values()) {
                    pw.printf("%d\t%s\t%s\t%s\t%s\t%.1f\t%s\n",
                            j.getJobId(),
                            clean(j.getJobTitle()),
                            clean(j.getCompanyName()),
                            clean(j.getRequiredSkillsCommaSeparated()),
                            clean(j.getPreferredSkillsCommaSeparated()),
                            j.getMinimumExperience(),
                            clean(j.getRequiredEducation()));
                }
            }
        } catch (Exception ignored) {}
    }

    private static synchronized void loadFallbackData() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) return;

            File candFile = new File(dir, "candidates_data.txt");
            if (candFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(candFile), StandardCharsets.UTF_8))) {
                    String line;
                    int maxId = 0;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\t", -1);
                        if (parts.length >= 10) {
                            int id = Integer.parseInt(parts[0]);
                            Candidate c = new Candidate();
                            c.setCandidateId(id);
                            c.setName(parts[1]);
                            c.setEmail(parts[2]);
                            c.setPhone(parts[3]);
                            c.setEducation(parts[4]);
                            c.setExperienceYears(Double.parseDouble(parts[5]));
                            c.setSkillsFromCommaSeparated(parts[6]);
                            c.setProjectsFromJoined(parts[7]);
                            c.setCertificationsFromJoined(parts[8]);
                            c.setResumePath(parts[9]);
                            fallbackCandidates.put(id, c);
                            maxId = Math.max(maxId, id);
                        }
                    }
                    candSeq.set(maxId + 1);
                }
            }

            File jobsFile = new File(dir, "jobs_data.txt");
            if (jobsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jobsFile), StandardCharsets.UTF_8))) {
                    String line;
                    int maxId = 0;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\t", -1);
                        if (parts.length >= 7) {
                            int id = Integer.parseInt(parts[0]);
                            Job j = new Job();
                            j.setJobId(id);
                            j.setJobTitle(parts[1]);
                            j.setCompanyName(parts[2]);
                            j.setRequiredSkillsFromCommaSeparated(parts[3]);
                            j.setPreferredSkillsFromCommaSeparated(parts[4]);
                            j.setMinimumExperience(Double.parseDouble(parts[5]));
                            j.setRequiredEducation(parts[6]);
                            fallbackJobs.put(id, j);
                            maxId = Math.max(maxId, id);
                        }
                    }
                    jobSeq.set(maxId + 1);
                }
            }
        } catch (Exception ignored) {}
    }

    private static String clean(String s) {
        if (s == null) return "";
        return s.replace("\t", " ").replace("\r", "").replace("\n", " ");
    }
}
