package model;

import java.io.File;

/**
 * Model representing a physical or imported Resume document.
 * Encapsulates file metadata, raw text content, and format details.
 */
public class Resume {
    private int resumeId;
    private String candidateName;
    private String filePath;
    private String fileFormat;
    private String rawContent;
    private long fileSizeBytes;

    public Resume() {
        this.candidateName = "";
        this.filePath = "";
        this.fileFormat = "TXT";
        this.rawContent = "";
    }

    public Resume(String candidateName, String filePath, String fileFormat, String rawContent) {
        this.candidateName = candidateName;
        this.filePath = filePath;
        this.fileFormat = fileFormat;
        this.rawContent = rawContent;
        if (filePath != null) {
            File f = new File(filePath);
            this.fileSizeBytes = f.exists() ? f.length() : 0;
        }
    }

    public Resume(int resumeId, String candidateName, String filePath, String fileFormat, String rawContent) {
        this(candidateName, filePath, fileFormat, rawContent);
        this.resumeId = resumeId;
    }

    public int getResumeId() {
        return resumeId;
    }

    public void setResumeId(int resumeId) {
        this.resumeId = resumeId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    @Override
    public String toString() {
        return String.format("Resume [ID=%d, File='%s', Format='%s', Size=%d bytes]",
                resumeId, filePath, fileFormat, fileSizeBytes);
    }
}
