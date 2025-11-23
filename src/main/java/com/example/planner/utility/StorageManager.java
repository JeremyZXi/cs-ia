package com.example.planner.utility;

import com.example.planner.module.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utility class use to manage file input and ouput for storage/tasks catalog data
 * <p>
 *     read setting JSON file and map it back to object
 *     map Setting object to JSON file
 * </p>
 */
public class StorageManager {
    private static final Path DIR = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("storage.json");


    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    /**
     * check if storage.json exists
     * @return whether storage file exists or not
     */
    public static boolean storageExists() {
        return Files.exists(FILE);
    }

    /**
     * save map of tasks object to the storage.json file
     * @param tasks map containing tasks with key being its UUID
     * @throws Exception from mapper
     */
    public static void save(Map<String, Task> tasks) throws Exception {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), tasks);
    }
    /**
     * read file into object
     * @return map of tasks from the file
     * @throws Exception
     */
    public static Map<String, Task> load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), new TypeReference<Map<String, Task>>() {
        });
    }
}
