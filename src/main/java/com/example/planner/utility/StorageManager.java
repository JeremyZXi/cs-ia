package com.example.planner.utility;

import com.example.planner.module.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class StorageManager {
    private static final Path DIR = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("storage.json");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static boolean storageExists() {
        return Files.exists(FILE);
    }

    public static void save(Map<String, Task> tasks) throws Exception {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), tasks);
    }

    public static ArrayList<Task> load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), new TypeReference<ArrayList<Task>>() {});
    }
}
