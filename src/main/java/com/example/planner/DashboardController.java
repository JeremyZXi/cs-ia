package com.example.planner;
import com.example.planner.module.Task;
import com.example.planner.utility.StorageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardController{
        @FXML
        private Label lblGreetings;

        @FXML
        private Label lblHeader;

        @FXML
        private Label lblTaskCat1;

        @FXML
        private Label lblTaskCat2;

        @FXML
        private VBox vboxAllTask;

        @FXML
        private VBox vboxSection;

        @FXML
        private VBox vboxTodayTask;

        private MasterController masterController;
        private Map<String, Task> tasks = new HashMap<>();

        public void initialize() throws Exception {
                masterController = MasterController.getInstance();
                try {
                        // Get shared data and handle null case
                        tasks = masterController.getSharedData("Tasks");
                        if (tasks == null) {
                                tasks = new HashMap<>();
                                masterController.setSharedData("Tasks", tasks);
                        }
                        /*
                        Task temp = new Task(LocalDate.now(), LocalTime.now(),LocalTime.now(),10,"Test Task","This is a test task");
                        tasks.put("task1",temp);
                        StorageManager.save(tasks);
                        */
                        System.out.println("Dashboard initialized successfully with " + tasks.size() + " tasks");
                } catch (Exception e) {
                        System.err.println("Error in DashboardController initialize: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Create a fallback empty task list to prevent further errors

                        if (tasks == null) {
                                tasks = new HashMap<>();
                        }
                }


        }

        @FXML
        public void handleAddTask(){
                masterController.openWindow("/com/example/planner/PopupSelection.fxml","Add New Tasks",null);

        }





}
