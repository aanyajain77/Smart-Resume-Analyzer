# SmartHire: Resume Analyzer and Job Matching System
**Course: Programming in Java (25CSE0202)**

---

## 📖 Project Overview

**SmartHire** is an enterprise-grade, console-based Java application engineered to automate the recruitment workflow:
- Ingest and parse candidate resumes from physical **TXT** (and optional PDF) files.
- Manage candidate profiles and job descriptions with permanent **SQLite** JDBC database persistence.
- Evaluate candidate compatibility using a **4-pillar dynamic weighted matching algorithm**:
  - **Technical Skills**: 40%
  - **Work Experience**: 25%
  - **Educational Qualifications**: 20%
  - **Projects & Certifications**: 15%
- Identify skill gaps with **Set-based classification** (HashSet, LinkedHashSet, TreeSet) and generate personalized career learning roadmaps.
- Rank candidate pools using **PriorityQueue Max-Heaps**, **TreeMaps**, **Comparators**, and **Java 8+ Stream API**.
- Process bulk resume uploads asynchronously using a **Producer-Consumer Multithreaded Architecture** (`wait()`, `notifyAll()`, `Thread`, `Runnable`, Daemon threads).
- Import and export candidate records and matching reports in **CSV** and **TXT** formats.

---

## 🎓 Syllabus Concepts & Practical Demonstration

| Java Syllabus Topic | How It Is Implemented in SmartHire | Key Classes / Files |
| :--- | :--- | :--- |
| **Encapsulation** | Private attributes, controlled getters and setters, defensive copies. | `model.Person`, `model.Candidate`, `model.Job`, `model.MatchResult` |
| **Inheritance** | `Candidate` extends abstract parent class `Person` using `super()` constructors. | `model.Candidate`, `model.Person` |
| **Polymorphism** | Overridden `displayDetails()` & `toString()`, overloaded `updateContact()` & constructors. | `model.Candidate`, `model.Person`, `service.TxtResumeParser` |
| **Abstraction** | Abstract class `Person` with abstract method contract `displayDetails()`. | `model.Person` |
| **Interfaces** | Service contract `ResumeParser` implemented polymorphically by `TxtResumeParser`. | `service.ResumeParser`, `service.TxtResumeParser` |
| **Arrays & Multidimensional Arrays** | `double[][] matchingScores` calculating matrix of all candidates against all jobs. | `service.MatchingService.calculateMatchingMatrix()` |
| **Strings & Text Processing** | `contains()`, `split()`, `equalsIgnoreCase()`, `trim()`, `replace()`, `toLowerCase()`, `StringBuilder`. | `service.TxtResumeParser`, `report.ReportGenerator` |
| **Wrapper Classes** | `Integer.parseInt()`, `Double.parseDouble()`, `Double.compare()`. | `service.TxtResumeParser`, `service.CandidateManager` |
| **Collections Framework** | `ArrayList`, `HashMap`, `LinkedList`, `HashSet`, `LinkedHashSet`, `TreeSet`, `TreeMap`, `PriorityQueue`. | `service.SkillExtractor`, `service.RankingService`, `service.CandidateManager` |
| **Iterators** | `Iterator` and `ListIterator` for list traversal, in-place update, and safe removal. | `service.CandidateManager` |
| **Custom Sorting** | `Comparable<Candidate>` (natural ID sorting) and `Comparator` (experience & name). | `model.Candidate`, `service.CandidateManager` |
| **Multithreading** | Producer-Consumer pattern using `Thread`, `Runnable`, `start()`, `run()`, `sleep()`, `join()`, `interrupt()`, Daemon threads, thread priorities, synchronized `wait()` / `notifyAll()`. | `multithreading.ResumeProcessor`, `multithreading.ResumeProducer`, `multithreading.ResumeConsumer` |
| **Database & JDBC** | SQLite connectivity, `Candidates`, `Jobs`, `MatchResults` tables, `PreparedStatement`, `Statement`, `ResultSet`, `try-with-resources`. | `database.DatabaseManager` |
| **Streams & Lambdas** | `filter()`, `map()`, `sorted()`, `distinct()`, `limit()`, `skip()`, `collect()`, `forEach()`, average and max score computations. | `service.RankingService` |
| **Generics** | Generic utility class `DataManager<T>` demonstrating type safety and diamond operator `<>`. | `util.DataManager<T>` |
| **File Handling** | `FileReader`, `FileWriter`, `BufferedReader`, `BufferedWriter`, `FileInputStream`, `FileOutputStream`, `StreamTokenizer`. | `service.TxtResumeParser`, `report.ReportGenerator` |
| **Custom Exceptions** | `InvalidResumeException`, `CandidateNotFoundException`, `JobNotFoundException`, `DuplicateCandidateException`, `InvalidInputException`. | `exceptions.*` |

---

## 📁 Package & Directory Architecture

```
SmartHire/
├── pom.xml                                      # Maven build configuration
├── run.bat                                      # 1-Click Windows Command Prompt runner
├── run.ps1                                      # 1-Click Windows PowerShell runner
├── README.md                                    # University Project Documentation
├── database/                                    # Permanent SQLite database location
│   ├── smarthire.db                             # SQLite database file
│   ├── candidates_data.txt                      # Fallback persistent candidate store
│   └── jobs_data.txt                            # Fallback persistent jobs store
├── resumes/                                     # Sample resume files for testing
│   ├── resume1_alice.txt                        # Senior Java Developer (6 yrs exp)
│   ├── resume2_bob.txt                          # Junior Developer (2 yrs exp)
│   ├── resume3_carol.txt                        # Data Engineer (4.5 yrs exp)
│   └── resume4_david.txt                        # Full Stack Engineer (3.5 yrs exp)
├── reports/                                     # Generated TXT and CSV export directory
│   ├── candidates_export.csv                    # CSV Candidate export
│   └── matching_results_export.csv              # CSV Evaluation export
└── src/main/java/
    ├── main/
    │   └── Main.java                            # Interactive 15-Option Console Menu
    ├── model/
    │   ├── Person.java                          # Abstract Base Person Entity
    │   ├── Candidate.java                       # Candidate Subclass (Comparable)
    │   ├── Job.java                             # Job Description Entity
    │   ├── Resume.java                          # Resume Metadata Entity
    │   └── MatchResult.java                     # Dynamic Match Evaluation Entity
    ├── service/
    │   ├── ResumeParser.java                    # Interface Contract for Parsers
    │   ├── TxtResumeParser.java                 # Concrete Parser Implementation
    │   ├── SkillExtractor.java                  # Set-based (TreeSet/HashSet) Extractor
    │   ├── MatchingService.java                 # 4-Pillar Scoring & double[][] Matrix
    │   ├── RecommendationService.java           # Skill Gap & Roadmap Generator
    │   ├── RankingService.java                  # PriorityQueue, TreeMap & Streams
    │   ├── CandidateManager.java                # Collections, Iterator & ListIterator
    │   └── JobManager.java                      # Job Collections & DB Sync
    ├── database/
    │   └── DatabaseManager.java                 # SQLite JDBC CRUD with PreparedStatement
    ├── multithreading/
    │   ├── ResumeProducer.java                  # Producer Task (Runnable)
    │   ├── ResumeConsumer.java                  # Consumer Task (Runnable)
    │   └── ResumeProcessor.java                 # Pipeline Coordinator (wait/notifyAll)
    ├── report/
    │   └── ReportGenerator.java                 # StringBuilder Reports & CSV I/O
    ├── exceptions/
    │   ├── InvalidResumeException.java          # Custom Exception
    │   ├── CandidateNotFoundException.java      # Custom Exception
    │   ├── JobNotFoundException.java            # Custom Exception
    │   ├── DuplicateCandidateException.java      # Custom Exception
    │   └── InvalidInputException.java           # Custom Exception
    └── util/
        ├── InputValidator.java                  # Regex & Range Validation
        └── DataManager.java                     # Generics Utility (DataManager<T>)
```

---

## 💻 How to Run the Application

### Method 1: One-Click Execution (Fastest & Recommended)

- **Via Windows Explorer / Command Prompt**:
  Double-click `run.bat` or run:
  ```cmd
  run.bat
  ```
- **Via Windows PowerShell**:
  ```powershell
  .\run.ps1
  ```

---

### Method 2: Running in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Click **File -> Open...** and select the project folder:
   `C:\Users\Lenovo\OneDrive\Desktop\Java Console project`
3. IntelliJ will recognize the `pom.xml` and import dependencies automatically.
4. In the Project pane, navigate to:
   `src/main/java/main/Main.java`
5. Right-click `Main.java` and select **Run 'Main.main()'** (or press `Shift + F10`).
6. The interactive console menu will appear in IntelliJ's **Run** tab.

---

### Method 3: Running in Eclipse IDE

1. Open **Eclipse IDE**.
2. Select **File -> Import... -> Maven -> Existing Maven Projects**.
3. Browse to the project folder and click **Finish**.
4. In Package Explorer, open:
   `src/main/java` -> `main` -> `Main.java`
5. Right-click `Main.java` -> **Run As -> Java Application**.
6. Interact with the application in Eclipse's **Console** view.

---

### Method 4: Terminal with Maven

```bash
# Compile project
mvn clean compile

# Run Main class
mvn exec:java

# Or build standalone JAR with all dependencies:
mvn package
java -jar target/SmartHire-1.0.0.jar
```

---

### Method 5: Plain Command Line (Using Java JDK directly)

Precompiled bytecode is available in `target/classes`. You can launch directly:

```powershell
# Run with Java runtime
java -cp target/classes main.Main

# To recompile if you modify code:
$files = (Get-ChildItem -Path src\main\java -Filter *.java -Recurse).FullName
javac -d target/classes $files
java -cp target/classes main.Main
```

---

## 🎮 Interactive Menu Walkthrough

```
========== SMART HIRE SYSTEM ==========

1. Add Candidate / Upload Resume
2. View All Candidates
3. Search Candidate
4. Update Candidate Information
5. Delete Candidate
6. Add Job Description
7. View All Job Descriptions
8. Analyze Resume Against Job
9. Recommend Missing Skills
10. Rank Candidates for a Job
11. View Matching Reports
12. Process Multiple Resumes
13. Import Candidates from CSV
14. Export Candidates and Reports to CSV
15. Exit

Enter your choice:
```

### Quick Test Walkthrough:
1. **Option 12 (`Process Multiple Resumes`)**:
   Type `resumes` and press Enter. Watch the **Producer-Consumer multithreading pipeline** launch with Producer, 2 Consumers, and a Daemon monitor, parsing all 4 resumes concurrently and persisting them into the database!
2. **Option 6 (`Add Job Description`)**:
   - Title: `Senior Java Developer`
   - Company: `TechCorp Systems`
   - Required Skills: `Java, Spring Boot, MySQL, Docker, REST API`
   - Preferred Skills: `Kubernetes, AWS, Kafka`
   - Min Exp: `5.0`
   - Education: `Bachelor's Degree`
3. **Option 8 (`Analyze Resume Against Job`)**:
   Select Candidate #1 and Job #1. Observe the **detailed 4-pillar score breakdown**, progress bars, matched skills, and recommendations.
4. **Option 9 (`Recommend Missing Skills`)**:
   Inspect candidate skill gaps sorted alphabetically via `TreeSet` alongside actionable learning paths.
5. **Option 10 (`Rank Candidates for a Job`)**:
   Choose Job #1:
   - Sub-option 1: Full leaderboard.
   - Sub-option 2: Filter by minimum score and experience with Stream API.
   - Sub-option 3: Retrieve top N candidates with `PriorityQueue`.
   - Sub-option 5: Render candidate $\times$ job matching matrix using `double[][] matchingScores`!
6. **Option 14 (`Export Candidates and Reports to CSV`)**:
   Exports structured CSVs to `reports/candidates_export.csv` and `reports/matching_results_export.csv`.
