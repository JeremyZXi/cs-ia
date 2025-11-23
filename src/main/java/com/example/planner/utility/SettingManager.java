package com.example.planner.utility;

import com.example.planner.module.Setting;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class use to manage file input and ouput for setting data
 * <p>
 *     read setting JSON file and map it back to object
 *     map Setting object to JSON file
 * </p>
 */
public class SettingManager {
    private static final Path DIR = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("setting.json");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());


    /**
     * check if setting.json exists
     * @return whether storage file exists or not
     */
    public static boolean storageExists() {
        return Files.exists(FILE);
    }

    /**
     * save Setting object to the setting.json file
     * @param setting Setting object to be stored
     * @throws Exception from mapper
     */
    public static void save(Setting setting) throws Exception {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), setting);
    }

    /**
     * read file into object
     * @return Setting object read from the setting.json file
     * @throws Exception
     */
    public static Setting load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), Setting.class);
    }
}
