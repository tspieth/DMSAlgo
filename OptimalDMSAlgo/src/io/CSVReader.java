package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    private String filePath;
    private String delimiter;

    // Constructor with default delimiter (semicolon)
    public CSVReader(String filePath) {
        this(filePath, ";"); // Common delimiter is ;
    }

    public CSVReader(String filePath, String delimiter) {
        this.filePath = filePath;
        this.delimiter = delimiter;
    }

    // reads the entire CSV file and returns a list of string arrays,
    // where each array represents a line in the CSV
    public List<String[]> readAll() throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                // Zeile aufteilen nach Trennzeichen
                String[] values = line.split(delimiter);
                records.add(values);
            }
        }

        return records;
    }

    // reads the CSV file line by line and processes each line using a provided
    // LineProcessor callback

    public void readLineByLine(LineProcessor processor) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                processor.process(values);
            }
        }
    }

    // Callback interface for processing lines of the CSV file
    public interface LineProcessor {
        void process(String[] values);
    }
}
