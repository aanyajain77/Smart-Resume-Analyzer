package service;

import exceptions.InvalidResumeException;
import model.Candidate;

import java.io.File;
import java.io.IOException;

/**
 * Interface contract for Resume Parsing.
 * Demonstrates:
 * 1. Interface abstraction and service contracts
 * 2. Polymorphic parser implementation (e.g. TxtResumeParser)
 */
public interface ResumeParser {
    /**
     * Parses an actual resume file and returns a populated Candidate object.
     */
    Candidate parse(File file) throws InvalidResumeException, IOException;

    /**
     * Extracts raw text from the specified file.
     */
    String extractRawText(File file) throws IOException;
}
