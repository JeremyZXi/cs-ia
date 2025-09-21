package com.example.planner;
import com.example.planner.ui.CustomDatePicker;
import com.example.planner.ui.TaskCard;
import com.example.planner.module.Task;
import com.example.planner.utility.StorageManager;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.time.LocalDate;
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
        private VBox vboxOptimized;

        @FXML
        private VBox vboxTodayTask;

        //detail pane(right)
        @FXML
        private WebView wvDescription;
        @FXML
        private CheckBox checkBoxIsComplete;
        @FXML
        private TextField txtFieldTaskName;
        @FXML
        private TextArea txtAreaDescription;
        @FXML
        private Label lblTaskInfo;

        private MasterController masterController;
        private Map<String, Task> tasks = new HashMap<>();
        
        // State management for current task display
        private Task currentDisplayedTask = null;
        
        // Event listeners to manage cleanup
        private ChangeListener<String> titleListener = null;
        private ChangeListener<String> descriptionListener = null;
        private EventHandler<ActionEvent> completionHandler = null;
        
        // Reusable Markdown components
        private WebEngine webEngine;
        private Parser markdownParser;
        private HtmlRenderer markdownRenderer;
        
        // Track TaskCard instances for updates
        private Map<String, TaskCard> taskCardMap = new HashMap<>();



        public void initialize() throws Exception {
                masterController = MasterController.getInstance();
                
                // Initialize Markdown components once
                MutableDataSet options = new MutableDataSet();
                markdownParser = Parser.builder(options).build();
                markdownRenderer = HtmlRenderer.builder(options).build();
                
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
                inbox();




        }

        @FXML
        public void handleAddTask(){
                //masterController.openWindow("/com/example/planner/PopupSelection.fxml","Add New Tasks",null);

                masterController.openWindow(
                        "/com/example/planner/PopupSelection.fxml",
                        "Add New Tasks",
                        () -> {
                                // callback runs AFTER the popup is closed
                                // Reload task list from shared data
                                tasks = masterController.getSharedData("Tasks");
                                inbox(); // refresh the UI
                        }
                );

        }
        @FXML
        public void handleInbox(){
                inbox();
        }

        private void inbox(){
                lblHeader.setText("Inbox");
                vboxAllTask.getChildren().clear();
                vboxTodayTask.getChildren().clear();
                vboxSection.getChildren().clear();
                
                // clear the task card mapping
                taskCardMap.clear();

                LocalDate today = LocalDate.now();

                for (Task task : tasks.values()) {
                        TaskCard card = new TaskCard(task, this::displayTaskDetail, this::handleTaskUpdateFromCard);
                        
                        // store the card reference for future updates
                        taskCardMap.put(task.getId(), card);
                        card.refreshDisplay();
                        
                        if (task.getDueDate() != null && task.getDueDate().equals(today)) {
                                vboxTodayTask.getChildren().add(card);
                        } else {
                                vboxAllTask.getChildren().add(card);
                        }
                }
                updateCompletionTask();
                optimizedTasks();
        }

        private void optimizedTasks(){

                //TODO: this method will be used to display auto-planned task at the top of the list
                //vboxOptimized.getChildren().add(new TaskCard(tasks.get("df056c8d-f50d-4376-99dc-94a1296c4ab1"), this::displayTaskDetail, this::handleTaskUpdateFromCard));
        }
        private void displayTaskDetail(Task task) {

                if (task == null) {
                        clearTaskDetail();
                        return;
                }
                
                // If same task is already displayed, no need to rebind
                if (currentDisplayedTask == task) {
                        return;
                }
                
                // clean up previous listeners to avoid memory leak
                cleanupEventListeners();
                
                // update current task reference
                currentDisplayedTask = task;
                
                // update UI components with task data
                txtFieldTaskName.setText(task.getTitle());
                txtAreaDescription.setText(task.getDescription());
                checkBoxIsComplete.setSelected(task.isComplete());
                lblTaskInfo.setText(task.getDueDate().toString()+" ("+task.getLetterDate()+" day)");

                // new event listeners
                setupEventListeners(task);
                
                // render initial md content
                renderMarkdown(task.getDescription());
        }
        
        private void cleanupEventListeners() {
                // Remove old listeners if they exist
                if (titleListener != null) {
                        txtFieldTaskName.textProperty().removeListener(titleListener);
                }
                if (descriptionListener != null) {
                        txtAreaDescription.textProperty().removeListener(descriptionListener);
                }
                if (completionHandler != null) {
                        checkBoxIsComplete.setOnAction(null);
                }
        }
        
        private void setupEventListeners(Task task) {

                //listen to title, description, completion

                titleListener = (obs, oldVal, newVal) -> {
                        task.setTitle(newVal);
                        refreshTaskCard(task);
                        saveTasksToStorage();
                };
                txtFieldTaskName.textProperty().addListener(titleListener);


                descriptionListener = (obs, oldVal, newVal) -> {
                        task.setDescription(newVal);
                        renderMarkdown(newVal);
                        saveTasksToStorage();
                };
                txtAreaDescription.textProperty().addListener(descriptionListener);


                completionHandler = e -> {
                        task.setComplete(checkBoxIsComplete.isSelected());
                        refreshTaskCard(task);
                        saveTasksToStorage();
                        //refresh the sorting when completion status changes
                        updateCompletionTask();
                };
                checkBoxIsComplete.setOnAction(completionHandler);
        }
        
        private void renderMarkdown(String markdownText) {
                if (markdownText == null) markdownText = "";
                
             webEngine = wvDescription.getEngine();
                Node document = markdownParser.parse(markdownText);
                String html = markdownRenderer.render(document);
                webEngine.loadContent(html, "text/html");
        }
        
        private void saveTasksToStorage() {
                try {
                        StorageManager.save(tasks);
                } catch (Exception e) {
                        System.err.println("Error saving tasks: " + e.getMessage());
                        e.printStackTrace();
                }
        }
        
        /**
         * refreshes the corresponding TaskCard when a task is updated
         */
        private void refreshTaskCard(Task task) {
                if (task == null || task.getId() == null) return;
                
                TaskCard card = taskCardMap.get(task.getId());
                if (card != null) {
                        card.refreshDisplay();
                }
        }
        
        /**
         * handle task updates from TaskCard(e.g., completion checkbox changes)
         * Updates the detail pane if this task is currently displayed
         */
        private void handleTaskUpdateFromCard(Task task) {
                if (task == null) return;
                
                // save the updated task to storage
                saveTasksToStorage();
                
                // if this task is currently displayed in the detail pane, refresh it
                if (currentDisplayedTask == task) {
                        refreshDetailPane(task);
                }
                
                // refresh the sorting when task completion status changes from TaskCard
                updateCompletionTask();
        }
        
        /**
         * refresh the detail pane to reflect the current task state
         * this is called when the task is updated from a TaskCard
         */
        private void refreshDetailPane(Task task) {
                if (task == null) return;
                
                // update the UI components without triggering listeners
                //  remove listeners to avoid infinite loops
                cleanupEventListeners();
                
                txtFieldTaskName.setText(task.getTitle());
                txtAreaDescription.setText(task.getDescription());
                checkBoxIsComplete.setSelected(task.isComplete());
                
                // reestablish listeners
                setupEventListeners(task);
                
                // update md rendering
                renderMarkdown(task.getDescription());
        }
        
        private void clearTaskDetail() {
                cleanupEventListeners();
                currentDisplayedTask = null;
                txtFieldTaskName.clear();
                txtAreaDescription.clear();
                checkBoxIsComplete.setSelected(false);
                wvDescription.getEngine().loadContent("", "text/html");
        }

        private void updateCompletionTask() {
                vboxTodayTask.getChildren().clear();
                vboxAllTask.getChildren().clear();

              
                taskCardMap.clear();

                ArrayList<Task> completeToday = new ArrayList<>();
                ArrayList<Task> completeAll = new ArrayList<>();
                LocalDate today = LocalDate.now();

                for (Task task : tasks.values()) {
                        TaskCard taskCard = new TaskCard(task, this::displayTaskDetail, this::handleTaskUpdateFromCard);
                        taskCardMap.put(task.getId(), taskCard);
                        taskCard.refreshDisplay();

                        boolean isDueToday = task.getDueDate() != null && task.getDueDate().equals(today);


                        if (isDueToday) {
                                if (task.isComplete()) {
                                        completeToday.add(task);
                                } else {
                                        vboxTodayTask.getChildren().add(taskCard);
                                }
                        } else {
                                if (task.isComplete()) {
                                        completeAll.add(task);
                                } else {
                                        vboxAllTask.getChildren().add(taskCard);
                                }
                        }
                }


                for (Task task : completeToday) {
                        TaskCard taskCard = taskCardMap.get(task.getId());
                        if (taskCard != null) {
                                vboxTodayTask.getChildren().add(taskCard);
                        }
                }

                // Add completed ALL OTHER tasks at bottom of all-task list
                for (Task task : completeAll) {
                        TaskCard taskCard = taskCardMap.get(task.getId());
                        if (taskCard != null) {
                                vboxAllTask.getChildren().add(taskCard);
                        }
                }
        }





}
