package com.example.planner.utility;

import com.example.planner.module.Setting;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;

public class SettingManager {
    private static final Path DIR = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("setting.json");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static boolean storageExists() {
        return Files.exists(FILE);
    }

    public static void save(Setting setting) throws Exception {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), setting);
    }

    public static Setting load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), Setting.class);
    }
}
