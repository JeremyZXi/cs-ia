package com.example.planner;

import com.example.planner.module.Task;
import com.example.planner.utility.StorageManager;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A controller class responsible for search window
 * <p>
 * search tasks
 * edit and update tasks
 */
public class SearchViewController {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, String> dueDateColumn;
    @FXML
    private TableColumn<Task, String> timeSpanColumn;
    @FXML
    private TableColumn<Task, String> priorityColumn;
    @FXML
    private TableColumn<Task, String> completedColumn;
    @FXML
    private TableColumn<Task, String> sectionColumn;

    // Task detail components
    @FXML
    private CheckBox checkBoxIsComplete;
    @FXML
    private TextField txtFieldTaskName;
    @FXML
    private Label lblTaskInfo;
    @FXML
    private TextArea txtAreaDescription;
    @FXML
    private WebView wvDescription;
    @FXML
    private ImageView prioritySign;

    private MasterController masterController;
    private Map<String, Task> tasks;
    private ObservableList<Task> allTasks;
    private ObservableList<Task> displayedTasks;

    // markdown stuff
    private WebEngine webEngine;
    private Parser markdownParser;
    private HtmlRenderer markdownRenderer;

    // display tasks
    private Task currentDisplayedTask = null;
    private ChangeListener<String> titleListener = null;
    private ChangeListener<String> descriptionListener = null;
    private EventHandler<ActionEvent> completionHandler = null;

    @FXML
    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        tasks = masterController.getSharedData("Tasks");

        // initialize markdown component
        MutableDataSet options = new MutableDataSet();
        markdownParser = Parser.builder(options).build();
        markdownRenderer = HtmlRenderer.builder(options).build();

        // convert map to list to put it into observable ArrayList for real time updates
        List<Task> taskList = new ArrayList<>(tasks.values());
        allTasks = FXCollections.observableArrayList(taskList);

        // display all the tasks
        displayedTasks = FXCollections.observableArrayList(allTasks);
        taskTable.setItems(displayedTasks);

        // config table
        setupTableColumns();

        // set up handler for selection
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        displayTaskDetail(newSelection);
                    } else {
                        clearTaskDetail();
                    }
                }
        );

        // webview-md stuff
        webEngine = wvDescription.getEngine();

        // clear
        clearTaskDetail();
    }

    /**
     * load data into table(columns) and format them correctly
     */
    private void setupTableColumns() {
        // config each cell with value
        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));
        sectionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSection().getName()));

        dueDateColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (task.getDueDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                return new SimpleStringProperty(task.getDueDate().format(formatter));
            }
            return new SimpleStringProperty("Not set");
        });

        timeSpanColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getTimeSpan())));

        priorityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.1f", cellData.getValue().getPriority())));


        completedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isComplete() ? "Yes" : "No"));
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText();
        if (searchTerm == null || searchTerm.isEmpty()) {
            // if search is empty, display all tasks
            displayedTasks.clear();
            displayedTasks.addAll(allTasks);
        } else {
            // linear search
            List<Task> searchResults = linearSearch(searchTerm);
            displayedTasks.clear();
            displayedTasks.addAll(searchResults);
        }
    }

    /**
     * Linear search implementation to find tasks that match the search term
     * @param searchTerm string that contain the search term
     */
    private List<Task> linearSearch(String searchTerm) {
        List<Task> results = new ArrayList<>();
        String lowerCaseSearch = searchTerm.toLowerCase();

        // Linear search through all tasks
        for (Task task : allTasks) {
            if (taskMatches(task, lowerCaseSearch)) {
                results.add(task);
            }
        }

        return results;
    }

    /**
     * Checks if a task matches the search criteria AKA compare
     * @param task the task to check
     * @param searchTerm criteria to check
     */
    private boolean taskMatches(Task task, String searchTerm) {
        //compare all the fields
        if (task.getTitle() != null &&
                task.getTitle().toLowerCase().contains(searchTerm)) {
            return true;
        }

        if (task.getDescription() != null &&
                task.getDescription().toLowerCase().contains(searchTerm)) {
            return true;
        }

        if (String.valueOf(task.getPriority()).contains(searchTerm)) {
            return true;
        }

        return task.getDueDate() != null &&
                task.getDueDate().toString().toLowerCase().contains(searchTerm);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // clear search and show all tasks
        searchField.clear();
        displayedTasks.clear();
        displayedTasks.addAll(allTasks);
    }

    /**
     * Display task details in the right panel
     * @param task task to display
     */
    private void displayTaskDetail(Task task) {
        if (task == null) {
            clearTaskDetail();
            return;
        }

        // if same task is already displayed, no need to rebind
        if (currentDisplayedTask == task) {
            return;
        }

        // clean up previous listeners
        cleanupEventListeners();

        //update current task reference
        currentDisplayedTask = task;

        //  UI components update
        txtFieldTaskName.setText(task.getTitle());
        txtAreaDescription.setText(task.getDescription());
        checkBoxIsComplete.setSelected(task.isComplete());

        // format ing
        String taskInfo = "";
        if (task.getDueDate() != null) {
            taskInfo = task.getDueDate().toString();
            if (task.getLetterDate() != 0) {
                taskInfo += " (" + task.getLetterDate() + " day)";
            }
            taskInfo += " | " + task.getTimeSpan() + " minutes";

            if (task.getSection() != null && task.getSection().getName() != null) {
                taskInfo += " | " + task.getSection().getName();
            }
        }
        lblTaskInfo.setText(taskInfo);

        // priority icon
        Image img = getPrioritySign(task);
        prioritySign.setImage(img);
        boolean show = (img != null);
        prioritySign.setVisible(show);
        prioritySign.setManaged(show);

        // event listeners
        setupEventListeners(task);

        // render initial markdown
        renderMarkdown(task.getDescription());
    }

    /**
     * clean up all the listeners to avoid memory leak
     */
    private void cleanupEventListeners() {
        // remove old listeners if they exist so no memory leak
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
    /**
     * setup event listener for the task displayed in the detail pane
     * @param task the task which we want to update
     */
    private void setupEventListeners(Task task) {
        // listen to title
        titleListener = (obs, oldVal, newVal) -> {
            task.setTitle(newVal);
            refreshTableView();
            saveTasksToStorage();
        };
        txtFieldTaskName.textProperty().addListener(titleListener);

        // listen to description
        descriptionListener = (obs, oldVal, newVal) -> {
            task.setDescription(newVal);
            renderMarkdown(newVal);
            saveTasksToStorage();
        };
        txtAreaDescription.textProperty().addListener(descriptionListener);

        // listen to completion status
        completionHandler = e -> {
            task.setComplete(checkBoxIsComplete.isSelected());
            refreshTableView();
            saveTasksToStorage();
        };
        checkBoxIsComplete.setOnAction(completionHandler);
    }

    /**
     * refresh table view to show updates
     */
    private void refreshTableView() {
        // table refresh to show updated values
        taskTable.refresh();
    }

    /**
     * convert markdown into html to display in the webengine
     * @param markdownText the markdown task to display
     */
    private void renderMarkdown(String markdownText) {
        if (markdownText == null) markdownText = "";

        Node document = markdownParser.parse(markdownText);
        String html = markdownRenderer.render(document);
        webEngine.loadContent(html, "text/html");
    }


    /**
     * save tasks to permanent storage by using StorageManager
     */
    private void saveTasksToStorage() {
        try {
            StorageManager.save(tasks);
        } catch (Exception e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clear detail pane for latter use
     */
    private void clearTaskDetail() {
        cleanupEventListeners();
        currentDisplayedTask = null;
        txtFieldTaskName.clear();
        txtAreaDescription.clear();
        checkBoxIsComplete.setSelected(false);
        lblTaskInfo.setText("No task selected");
        wvDescription.getEngine().loadContent("", "text/html");
        prioritySign.setImage(null);
        prioritySign.setVisible(false);
        prioritySign.setManaged(false);
    }

    /**
     * correspond priority to the corresponding UI component
     * @param task corresponding task object
     */
    private Image getPrioritySign(Task task) {
        double p = task.getPriority();
        final double EPS = 1e-6;

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
}
