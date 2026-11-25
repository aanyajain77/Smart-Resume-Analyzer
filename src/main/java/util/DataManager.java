package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic Data Management utility class demonstrating:
 * 1. Java Generics (Type parameter <T>)
 * 2. Type safety at compile time
 * 3. Generic collections and Diamond operator (<>)
 * 4. Generic methods with bounded or unbounded type parameters
 */
public class DataManager<T> {
    private final List<T> records;
    private final String entityName;

    // Default Constructor demonstrating Diamond operator <>
    public DataManager(String entityName) {
        this.entityName = entityName != null ? entityName : "Record";
        this.records = new ArrayList<>();
    }

    // Add Record
    public void addRecord(T record) {
        if (record != null) {
            records.add(record);
        }
    }

    // Retrieve Record by Index
    public T getRecord(int index) {
        if (index >= 0 && index < records.size()) {
            return records.get(index);
        }
        return null;
    }

    // Retrieve All Records as an unmodifiable list
    public List<T> getAllRecords() {
        return Collections.unmodifiableList(records);
    }

    // Remove Record
    public boolean removeRecord(T record) {
        return records.remove(record);
    }

    // Remove Record by Index
    public T removeRecord(int index) {
        if (index >= 0 && index < records.size()) {
            return records.remove(index);
        }
        return null;
    }

    // Display Records
    public void displayRecords() {
        System.out.printf("\n--- Displaying All %s Records (%d total) ---\n", entityName, records.size());
        if (records.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        int index = 1;
        for (T record : records) {
            System.out.printf("[%d] %s\n", index++, record.toString());
        }
    }

    // Count of records
    public int getCount() {
        return records.size();
    }

    // Clear records
    public void clear() {
        records.clear();
    }

    /**
     * Generic static method demonstrating type parameter <E>
     * Reusable for printing any collection of items.
     */
    public static <E> void printElements(List<E> list, String header) {
        System.out.println("=== " + header + " ===");
        if (list == null || list.isEmpty()) {
            System.out.println("  (List is empty)");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("  %d. %s\n", (i + 1), list.get(i));
        }
    }
}
