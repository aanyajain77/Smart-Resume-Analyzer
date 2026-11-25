package service;

import database.DatabaseManager;
import exceptions.JobNotFoundException;
import model.Job;

import java.sql.SQLException;
import java.util.*;

/**
 * Service managing Job Descriptions.
 * Synchronizes between in-memory collections and SQLite database.
 */
public class JobManager {

    private final List<Job> jobList;
    private final Map<Integer, Job> jobMapById;

    public JobManager() {
        this.jobList = new ArrayList<>();
        this.jobMapById = new HashMap<>();
        refreshFromDatabase();
    }

    public synchronized void refreshFromDatabase() {
        jobList.clear();
        jobMapById.clear();
        List<Job> dbJobs = DatabaseManager.getAllJobs();
        for (Job j : dbJobs) {
            jobList.add(j);
            jobMapById.put(j.getJobId(), j);
        }
    }

    public int addJob(Job job) throws SQLException {
        int id = DatabaseManager.insertJob(job);
        if (id > 0) {
            job.setJobId(id);
            jobList.add(job);
            jobMapById.put(id, job);
        }
        return id;
    }

    public List<Job> getAllJobs() {
        refreshFromDatabase();
        return new ArrayList<>(jobList);
    }

    public Job searchById(int jobId) throws JobNotFoundException {
        Job j = jobMapById.get(jobId);
        if (j != null) return j;
        return DatabaseManager.getJobById(jobId);
    }

    public List<Job> searchByTitle(String titleKeyword) {
        List<Job> matches = new ArrayList<>();
        if (titleKeyword == null || titleKeyword.trim().isEmpty()) return matches;
        String lower = titleKeyword.trim().toLowerCase();
        for (Job j : jobList) {
            if (j.getJobTitle().toLowerCase().contains(lower) || j.getCompanyName().toLowerCase().contains(lower)) {
                matches.add(j);
            }
        }
        return matches;
    }

    public boolean updateJob(Job job) throws JobNotFoundException {
        if (!jobMapById.containsKey(job.getJobId())) {
            throw new JobNotFoundException("Job ID #" + job.getJobId() + " not found for update.");
        }
        boolean ok = DatabaseManager.updateJob(job);
        if (ok) {
            for (int i = 0; i < jobList.size(); i++) {
                if (jobList.get(i).getJobId() == job.getJobId()) {
                    jobList.set(i, job);
                    break;
                }
            }
            jobMapById.put(job.getJobId(), job);
            return true;
        }
        return false;
    }

    public boolean deleteJob(int jobId) throws JobNotFoundException {
        boolean ok = DatabaseManager.deleteJob(jobId);
        if (ok) {
            jobList.removeIf(j -> j.getJobId() == jobId);
            jobMapById.remove(jobId);
            return true;
        }
        throw new JobNotFoundException("Job ID #" + jobId + " could not be deleted.");
    }

    public void displayJobsTable(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            System.out.println("[!] No job descriptions found.");
            return;
        }

        System.out.println("=".repeat(105));
        System.out.printf("%-5s | %-25s | %-20s | %-9s | %-35s\n",
                "ID", "Job Title", "Company", "Min Exp", "Required Skills");
        System.out.println("-".repeat(105));

        for (Job j : jobs) {
            System.out.printf("%-5d | %-25s | %-20s | %-9.1f | %-35s\n",
                    j.getJobId(),
                    truncate(j.getJobTitle(), 25),
                    truncate(j.getCompanyName(), 20),
                    j.getMinimumExperience(),
                    truncate(j.getRequiredSkillsCommaSeparated(), 35));
        }
        System.out.println("=".repeat(105));
        System.out.printf("Total Job Descriptions: %d\n", jobs.size());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
