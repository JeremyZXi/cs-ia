package com.example.planner;
import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.ui.CustomDatePicker;
import com.example.planner.ui.TaskCard;
import com.example.planner.module.Task;
import com.example.planner.utility.Planning;
import com.example.planner.utility.StorageManager;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController{
        @FXML
        private ImageView prioritySign;
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
        @FXML
        private Button btnPlan;

        private MasterController masterController;
        private Map<String, Task> tasks = new HashMap<>();

        private List<String[]> data = readCSV("data/letter_day_calendar.csv");
        
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

        // use to keep result of the auto planning
        private boolean optimizedActive = false;               // whether the user planned their task
        private boolean onInbox = true;
        private ArrayList<String> optimizedTaskIds = new ArrayList<>(); // store the order and id of planned task




        private Setting setting;
        private Section selectedSection;





        public void initialize() throws Exception {
                masterController = MasterController.getInstance();
                
                // Initialize Markdown components once
                MutableDataSet options = new MutableDataSet();
                markdownParser = Parser.builder(options).build();
                markdownRenderer = HtmlRenderer.builder(options).build();
                
                try {
                        // Get shared data and handle null case
                        tasks = masterController.getSharedData("Tasks");
                        setting = masterController.getSharedData("setting");
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
                Stage currentStage = (Stage) btnPlan.getScene().getWindow();

                masterController.openWindow("/com/example/planner/PopupSelection.fxml", "Add New Tasks", () -> {
                                // callback runs AFTER the popup is closed
                                // Reload task list from shared data
                                tasks = masterController.getSharedData("Tasks");
                                inbox(); // refresh the UI
                        },currentStage
                );


        }
        @FXML
        public void handleInbox(){
                inbox();
        }

        @FXML
        public void handleToday(){
                vboxSection.getChildren().clear();
                LocalDate today = LocalDate.now();
                for (Section section:setting.getSections()){
                        for(String letter: section.getLetterDates()){
                                if (letter.equals(letterDate(today))){
                                        System.out.println(section.getName());

                                        Button sectionBtn = new Button(section.getName());
                                        sectionBtn.setOnAction(e->{
                                                btnPlan.setDisable(false);
                                                optimizedActive = false;
                                                onInbox = false;
                                                optimizedTaskIds.clear();

                                                taskCardMap.clear();
                                                selectedSection = section;
                                                lblHeader.setText(section.getName());
                                                vboxAllTask.getChildren().clear();
                                                vboxTodayTask.getChildren().clear();
                                                //filter tasks
                                                Map<String,Task> selectedTasks = filterTask(tasks,section);
                                                for(Task task:selectedTasks.values()){
                                                        TaskCard card = new TaskCard(task, this::displayTaskDetail, this::handleTaskUpdateFromCard);
                                                        taskCardMap.put(task.getId(), card);
                                                        card.refreshDisplay();
                                                        if (task.getDueDate() != null && task.getDueDate().equals(today)) {
                                                                vboxTodayTask.getChildren().add(card);
                                                        } else if (task.getDueDate().isBefore(today)) {
                                                                vboxTodayTask.getChildren().add(card);
                                                                System.out.println("past due 1");
                                                        } else {
                                                                vboxAllTask.getChildren().add(card);
                                                        }
                                                        renderLists(selectedTasks);
                                                        //optimizedTasks();
                                                }
                                        });
                                        vboxSection.getChildren().add(sectionBtn);
                                }
                        }
                }
        }
        private void inbox(){
                onInbox = true;
                LocalDate today = LocalDate.now();

                selectedSection = null;
                optimizedActive = false;
                optimizedTaskIds.clear();

                vboxAllTask.getChildren().clear();
                vboxTodayTask.getChildren().clear();
                vboxSection.getChildren().clear();

                btnPlan.setDisable(true);
                lblHeader.setText("inbox");




                //TODO: section selection
                for (Section section:setting.getSections()){
                        Button sectionBtn = new Button(section.getName());
                        sectionBtn.setOnAction(e->{
                                btnPlan.setDisable(false);
                                optimizedActive = false;
                                onInbox = false;
                                optimizedTaskIds.clear();

                                taskCardMap.clear();
                                selectedSection = section;
                                lblHeader.setText(section.getName());
                                vboxAllTask.getChildren().clear();
                                vboxTodayTask.getChildren().clear();
                                //filter tasks
                                Map<String,Task> selectedTasks = filterTask(tasks,section);
                                for(Task task:selectedTasks.values()){
                                        TaskCard card = new TaskCard(task, this::displayTaskDetail, this::handleTaskUpdateFromCard);
                                        taskCardMap.put(task.getId(), card);
                                        card.refreshDisplay();
                                        if (task.getDueDate() != null && task.getDueDate().equals(today)) {
                                                vboxTodayTask.getChildren().add(card);
                                        } else if (task.getDueDate().isBefore(today)) {
                                                vboxTodayTask.getChildren().add(card);
                                                System.out.println("past due 1");
                                        } else {
                                                vboxAllTask.getChildren().add(card);
                                        }
                                        renderLists(selectedTasks);
                                        //optimizedTasks();
                                }
                        });
                        vboxSection.getChildren().add(sectionBtn);
                }

                
                // clear the task card mapping
                taskCardMap.clear();



                for (Task task : tasks.values()) {
                        TaskCard card = new TaskCard(task, this::displayTaskDetail, this::handleTaskUpdateFromCard);
                        
                        // store the card reference for future updates
                        taskCardMap.put(task.getId(), card);
                        card.refreshDisplay();
                        
                        if (task.getDueDate() != null && task.getDueDate().equals(today)) {
                                vboxTodayTask.getChildren().add(card);
                        } else if(task.getDueDate().isBefore(today)){
                                vboxTodayTask.getChildren().add(card);
                                System.out.println("past due 2");
                        } else {
                                vboxAllTask.getChildren().add(card);
                        }
                }
                renderLists(tasks);
                //optimizedTasks();
        }

        @FXML
        public void OnOptimizedTasks() {
                LocalDate today = LocalDate.now();

                Map<String, Task> todayTasks = tasks.entrySet().stream()
                        .filter(e -> e.getValue().getDueDate() != null
                                && e.getValue().getDueDate().equals(today)
                                || e.getValue().getDueDate().isBefore(today)
                                && !e.getValue().isComplete())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                if (todayTasks.isEmpty()) {
                        optimizedActive = false;
                        optimizedTaskIds.clear();
                        renderLists(getActiveSource());
                        return;
                }

                int availableTime = 75; // TODO: obtain from settings
                ArrayList<Task> plannedTasks = Planning.plan(todayTasks, availableTime);

                // save the optimized tasks
                optimizedActive = true;
                optimizedTaskIds.clear();
                for (Task t : plannedTasks) {
                        optimizedTaskIds.add(t.getId());
                }

                // render normally first
                renderLists(getActiveSource());
                // apply the separators
                applyOptimizedLayout();
        }




        private Map<String, Task> filterTask(Map<String, Task> taskLists,Section filterSection){
                Map<String, Task> filteredTask = new HashMap<>();
                for (Map.Entry<String, Task> entry : taskLists.entrySet()) {
                        Task task = entry.getValue();
                        if (task != null && task.getSection() != null && task.getSection().getName() != null) {
                                if(task.getSection().getName().equals(filterSection.getName())){
                                        filteredTask.put(entry.getKey(), task);
                                } else {
                                    //skip this aka not include
                                }
                        } else {
                                //skip this aka not include
                        }
                }

                System.out.println(filteredTask);
                return  filteredTask;
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
                lblTaskInfo.setText(task.getDueDate().toString()+" ("+task.getLetterDate()+" day) | "+task.getTimeSpan()+" minutes | "+task.getSection().getName());
                Image img = getPrioritySign(task);
                prioritySign.setImage(img);
                boolean show = (img != null);
                prioritySign.setVisible(show);
                prioritySign.setManaged(show);


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
                        renderLists(getActiveSource());
                        //if (optimizedActive && !onInbox) {applyOptimizedLayout();}
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
                renderLists(getActiveSource());
                if (optimizedActive) {applyOptimizedLayout();}
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
                Image img = getPrioritySign(task);
                prioritySign.setImage(img);
                boolean show = (img != null);
                prioritySign.setVisible(show);
                prioritySign.setManaged(show);


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
                prioritySign.imageProperty().set(null);
        }

        private void renderLists(Map<String, Task> source) {
                vboxTodayTask.getChildren().clear();
                vboxAllTask.getChildren().clear();
                taskCardMap.clear();

                LocalDate today = LocalDate.now();
                ArrayList<Task> completeToday = new ArrayList<>();
                ArrayList<Task> completeAll = new ArrayList<>();

                for (Task task : source.values()) {
                        TaskCard card = new TaskCard(task, this::displayTaskDetail, this::handleTaskUpdateFromCard);
                        taskCardMap.put(task.getId(), card);
                        card.refreshDisplay();

                        // ★ Treat dueDate <= today as "Today's Tasks"
                        boolean dueKnown = task.getDueDate() != null;
                        boolean isTodayOrPast = dueKnown && !task.getDueDate().isAfter(today);

                        if (isTodayOrPast) {
                                if (task.isComplete()) {
                                        completeToday.add(task);      // completed overdue/today go to bottom of Today box
                                } else {
                                        vboxTodayTask.getChildren().add(card);
                                }
                        } else {
                                if (task.isComplete()) {
                                        completeAll.add(task);
                                } else {
                                        vboxAllTask.getChildren().add(card);
                                }
                        }
                }

                // Append completed at the bottom of each list
                for (Task t : completeToday) {
                        TaskCard c = taskCardMap.get(t.getId());
                        if (c != null) vboxTodayTask.getChildren().add(c);
                }
                for (Task t : completeAll) {
                        TaskCard c = taskCardMap.get(t.getId());
                        if (c != null) vboxAllTask.getChildren().add(c);
                }
        }






        private Image getPrioritySign(Task task){
                double p = task.getPriority();
                final double EPS = 1e-6; // handle floating point precision

                if (Math.abs(p - 1.0) < EPS) {
                        return new Image(getClass()
                                .getResource("/com/example/planner/icon/priority_regular.png")
                                .toExternalForm());
                } else if (Math.abs(p - 5.0) < EPS) {
                        return new Image(getClass()
                                .getResource("/com/example/planner/icon/priority_high.png")
                                .toExternalForm());
                } else if (Math.abs(p - 2.5) < EPS) {
                        return new Image(getClass()
                                .getResource("/com/example/planner/icon/priority_medium.png")
                                .toExternalForm());
                } else if (Math.abs(p - 0.0) < EPS) {
                        return new Image(getClass()
                                .getResource("/com/example/planner/icon/priority_low.png")
                                .toExternalForm());
                } else {
                        return null;
                }
        }
        //return map we should redner now(either selectedSection or other)
        private Map<String, Task> getActiveSource() {
                if (selectedSection == null){
                        return tasks;
                }
                return filterTask(tasks, selectedSection);
        }

        private void applyOptimizedLayout() {
                // if there's nothing, remove all the separators
                if (!optimizedActive || optimizedTaskIds == null || optimizedTaskIds.isEmpty()) {
                        removeOptimizedSeparators();
                        return;
                }

                // to avoid duplication
                removeOptimizedSeparators();

                // filter tasks: today(in case it's been a day & optimized)
                LocalDate today = LocalDate.now();
                ArrayList<String> stillApplicable = new ArrayList<>();
                for (String id : optimizedTaskIds) {
                        Task t = tasks.get(id);
                        if (t != null && t.getDueDate() != null
                                && (t.getDueDate().equals(today)
                                || t.getDueDate().isBefore(today))
                                && !t.isComplete()) {
                                stillApplicable.add(id);
                        }
                }

                // if all complete, quit
                if (stillApplicable.isEmpty()) {
                        optimizedActive = false;
                        optimizedTaskIds.clear();
                        return;
                }

                // insert top separator
                Label topLabel = new Label("Recommended Tasks (Optimized)");
                topLabel.getStyleClass().add("optimized-header");
                vboxTodayTask.getChildren().add(0, topLabel);

                // order the tasks
                int insertIndex = 1;
                for (String id : stillApplicable) {
                        TaskCard card = taskCardMap.get(id);
                        if (card != null) {
                                vboxTodayTask.getChildren().remove(card);              // remove from original place
                                vboxTodayTask.getChildren().add(insertIndex, card);    // insert to top
                                insertIndex++;
                        }
                }

                // buttom separator
                Label bottomLabel = new Label("Other Tasks");
                bottomLabel.getStyleClass().add("optimized-footer");
                vboxTodayTask.getChildren().add(insertIndex, bottomLabel);

                // update  optimizedTaskIds to stillApplicable to prevent past due/completed
                optimizedTaskIds = stillApplicable;
        }

        private void removeOptimizedSeparators() {
                vboxTodayTask.getChildren().removeIf(node ->
                        node instanceof Label &&
                                (
                                        "Recommended Tasks (Optimized)".equals(((Label) node).getText()) ||
                                                "Other Tasks".equals(((Label) node).getText())
                                )
                );
        }

        //temporary date convertion
        //TODO: implement whatever Mr.Ben implemented in his VB code

        private String letterDate(LocalDate d) {
                String letter = "0";
                for (String[] row : data) {
                        if (row[0].equals(d.toString())) {
                                letter = String.valueOf(row[2].charAt(0));
                        }
                }
                return letter;
        }
        private List<String[]> readCSV(String file) {
                List<String[]> allData = null;
                try {
                        FileReader filereader = new FileReader(file);
                        CSVReader csvReader = new CSVReaderBuilder(filereader)
                                .withSkipLines(1)
                                .build();
                        allData = csvReader.readAll();

                } catch (Exception e) {
                        e.printStackTrace();
                }
                return allData;
        }





        //navbar navigation

        @FXML
        public void onSetting(){
                Stage currentStage = (Stage) btnPlan.getScene().getWindow();
                masterController.closeWindow("Dashboard");
                masterController.openWindow("/com/example/planner/Setting.fxml", "Setting", null,currentStage);
        }

        @FXML
        public void onCalendar(){
                Stage currentStage = (Stage) btnPlan.getScene().getWindow();

                masterController.openWindow("/com/example/planner/Calendar.fxml", "Calendar", null,null);
                masterController.closeWindow("Dashboard");
                System.out.println("Closing");
        }

        @FXML
        public void onDashboard(){
                Stage currentStage = (Stage) btnPlan.getScene().getWindow();

                masterController.openWindow("/com/example/planner/Dashboard.fxml", "Dashboard", null,null);
                masterController.closeWindow("Dashboard");
        }








}
