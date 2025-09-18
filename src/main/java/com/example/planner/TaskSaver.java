package com.example.planner;

import com.example.planner.module.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class TaskSaver {

    public static void saveTasksToJson(ArrayList<Task> tasks, String filePath) {
        ObjectMapper mapper = new ObjectMapper();

        // Enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Register module to support Java 8 time (LocalDate, etc.)


        try {
            mapper.writeValue(new File(filePath), tasks);
            System.out.println("Saved tasks to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

