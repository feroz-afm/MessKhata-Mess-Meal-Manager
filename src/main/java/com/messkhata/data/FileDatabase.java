package com.messkhata.data;

import com.messkhata.model.Storable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Very small file database. Each table is a text file, each record is one line,
 * fields are separated by "|".
 */
public class FileDatabase {

    private final Path folder;

    public FileDatabase(String folderName) {
        this.folder = Paths.get(folderName);
    }

    public Path getFolder() {
        return folder;
    }

    public boolean exists(String fileName) {
        return Files.exists(folder.resolve(fileName));
    }

    public List<String[]> read(String fileName) throws IOException {
        List<String[]> rows = new ArrayList<>();
        Path file = folder.resolve(fileName);
        if (!Files.exists(file)) {
            return rows;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.trim().isEmpty()) {
                rows.add(line.split(Pattern.quote(Storable.SEPARATOR), -1));
            }
        }
        return rows;
    }

    public void write(String fileName, List<String> lines) throws IOException {
        Files.createDirectories(folder);
        Files.write(folder.resolve(fileName), lines, StandardCharsets.UTF_8);
    }

    public void writeAll(String fileName, List<? extends Storable> items) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Storable s : items) {
            lines.add(s.toLine());
        }
        write(fileName, lines);
    }

    /** Removes the separator from user text so it cannot break a record. */
    public static String clean(String text) {
        return text == null ? "" : text.replace(Storable.SEPARATOR, "/").trim();
    }
}
