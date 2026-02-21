package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVWriter {

    private final File file;
    private final String delimiter;

    public CSVWriter(String filePath) {
        this(filePath, ";");
    }

    public CSVWriter(String filePath, String delimiter) {
        this.file = new File(filePath);
        this.delimiter = delimiter;
    }

    // Kopfzeile schreiben (überschreibt Datei)
    public void writeHeader(List<String> header) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(formatRow(header));
            writer.newLine();
        }
    }

    // Eine Zeile anhängen
    public void appendRow(List<String> row) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            writer.write(formatRow(row));
            writer.newLine();
        }
    }

    // Mehrere Zeilen anhängen
    public void appendRows(List<List<String>> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            for (List<String> row : rows) {
                writer.write(formatRow(row));
                writer.newLine();
            }
        }
    }

    // CSV-konforme Formatierung
    private String formatRow(List<String> row) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < row.size(); i++) {
            sb.append(escape(row.get(i)));

            if (i < row.size() - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    // Sonderzeichen korrekt escapen
    private String escape(String value) {
        if (value == null)
            return "";

        boolean containsSpecial = value.contains(delimiter) ||
                value.contains("\"") ||
                value.contains("\n") ||
                value.contains("\r");

        if (containsSpecial) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
