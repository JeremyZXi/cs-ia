package com.example.planner;

import com.example.planner.module.Task;
import com.example.planner.ui.TaskCalendarCard;
import com.example.planner.utility.Date2Letter;
import com.example.planner.utility.StorageManager;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * A controller class responsible for the UI components of the Calendar window, as well as some data processing
 * <p>
 * update UI components and sync them with data, vice versa
 * generate/display calendar part
 * edit tasks, display task cards, involve optimization method
 */
public class CalendarController implements Initializable {

    //fx:id for UI components
    // root of the stage in case
    @FXML
    private BorderPane root;



    @FXML
    private ComboBox<String> cmbMonth;   // visible month dropdown
    @FXML
    private Spinner<Integer> spnYear;    // year spinner

    // Calendar grid
    @FXML
    private GridPane gridMonth;

    // Right pane (task details)
    @FXML
    private CheckBox checkBoxIsComplete;
    @FXML
    private TextField txtFieldTaskName;
    @FXML
    private Label lblTaskInfo;
    @FXML
    private ImageView prioritySign;
    @FXML
    private TextArea txtAreaDescription;
    @FXML
    private WebView wvDescription;

    // state
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private boolean updatingControls = false;
    private MasterController masterController;
    private Map<String, Task> tasks = new HashMap<>();

    // detail-pane / markdown state
    private Task currentDisplayedTask = null;
    private ChangeListener<String> titleListener = null;
    private ChangeListener<String> descriptionListener = null;
    private EventHandler<ActionEvent> completionHandler = null;

    private WebEngine webEngine;
    private Parser markdownParser;
    private HtmlRenderer markdownRenderer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            masterController = MasterController.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // get shared tasks
        tasks = masterController.getSharedData("Tasks");


        // init markdown
        MutableDataSet options = new MutableDataSet();
        markdownParser = Parser.builder(options).build();
        markdownRenderer = HtmlRenderer.builder(options).build();
        webEngine = wvDescription.getEngine();

        currentYearMonth = YearMonth.now();

        setupMonthCombo();
        setupYearSpinner();

        // init sync of controls & calendar
        syncControlsFromYearMonth();
        buildCalendar();
    }



    /**
     * setup combo for month selection, jan to dec
     */
    private void setupMonthCombo() {
        cmbMonth.getItems().clear();
        //add all the month
        for (Month m : Month.values()) {
            String raw = m.name().toLowerCase();
            String name = Character.toUpperCase(raw.charAt(0)) + raw.substring(1); // January, February...
            cmbMonth.getItems().add(name);
        }
        //event listener for click
        cmbMonth.setOnAction(e -> {
            if (updatingControls) return;
            updateCalendarFromControls();
        });
    }
    /**
     * setup combo for year selection. Assumes a fixed range to reduce time complexity
     */
    private void setupYearSpinner() {
        int currentYear = currentYearMonth.getYear();
        //use fixed range to reduce time complexity in exchange with some memory space
        SpinnerValueFactory<Integer> yearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2500, currentYear);
        spnYear.setValueFactory(yearFactory);

        //set up listener to update calendar
        spnYear.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingControls) return;
            updateCalendarFromControls();
        });
    }



    /**
     * build calendar to UI
     */
    private void buildCalendar() {
        gridMonth.getChildren().clear();

        YearMonth ym = currentYearMonth;
        LocalDate firstOfMonth = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();

        // java time: Monday=1 ... Sunday=7
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1-7
        int firstColumn = firstDayOfWeek - 1; // 0-6 (Mon-Sun)

        LocalDate today = LocalDate.now();

        //add all the days
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);
            int cellIndex = firstColumn + (day - 1);
            int row = cellIndex / 7;
            int col = cellIndex % 7;

            VBox dayCell = createDayCell(date, today);
            populateTasksForDay(dayCell, date);

            GridPane.setRowIndex(dayCell, row);
            GridPane.setColumnIndex(dayCell, col);
            gridMonth.getChildren().add(dayCell);
        }
    }

    /**
     * create day cell that can contain tasks and other component
     * @param date date of that cell
     * @param today today's date to highlight
     * @return box return VBox of today
     */
    private VBox createDayCell(LocalDate date, LocalDate today) {
        VBox box = new VBox();
        box.setSpacing(4);
        box.setStyle("-fx-padding: 4; -fx-background-color: white;");

        Label lblDayNumber = new Label(
                date.getDayOfMonth() + " | " + Date2Letter.letterDate(date)
        );

        // highlight today
        if (date.equals(today)) {
            box.setStyle("-fx-padding: 4; -fx-background-color: #e3f2fd; -fx-border-color: #2196f3;");
        }

        box.getChildren().add(lblDayNumber);



        return box;
    }

    /**
     * add TaskCalendarCard components to a given day cell for all tasks due on that date
     * @param dayCell VBox of a date
     * @param date date of the cell
     */
    private void populateTasksForDay(VBox dayCell, LocalDate date) {
        if (tasks == null || tasks.isEmpty()) return;

        for (Task task : tasks.values()) {
            if (task == null || task.getDueDate() == null) continue;

            if (date.equals(task.getDueDate())) {
                TaskCalendarCard card = new TaskCalendarCard(task, this::onTaskClicked);
                dayCell.getChildren().add(card);
            }
        }
    }


    /**
     * involve method to display task details
     * @param task task to display
     */
    private void onTaskClicked(Task task) {
        if (task == null) return; //prevent null
        selectedDate = task.getDueDate();
        displayTaskDetail(task);   // same as Dashboard
    }

    /**
     * display tasks in detail pane
     * @param task task to display
     */
    private void displayTaskDetail(Task task) {
        //prevent null
        if (task == null) {
            clearTaskDetail();
            return;
        }

        // prevent repeat display
        if (currentDisplayedTask == task) {
            return;
        }

        // clean up previous listener so no memory leak
        cleanupEventListeners();

        // update current task reference
        currentDisplayedTask = task;

        // update UI components with task data
        txtFieldTaskName.setText(task.getTitle());
        txtAreaDescription.setText(task.getDescription());
        checkBoxIsComplete.setSelected(task.isComplete());

        if (task.getDueDate() != null) {
            lblTaskInfo.setText(
                    task.getDueDate().toString() + " (" + task.getLetterDate() + " day) | " +
                            task.getTimeSpan() + " minutes | " +
                            (task.getSection() != null ? task.getSection().getName() : "")
            );
        } else {
            lblTaskInfo.setText(" ");
        }

        Image img = getPrioritySign(task);
        prioritySign.setImage(img);
        boolean show = (img != null);
        prioritySign.setVisible(show);
        prioritySign.setManaged(show);

        // new event listeners
        setupEventListeners(task);

        // render init markdown content
        renderMarkdown(task.getDescription());
    }

    /**
     * clean up listener to prevent memory leak
     */
    private void cleanupEventListeners() {
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
     * setup listener for task displayed in the detail pane
     * @param task task displayed
     */
    private void setupEventListeners(Task task) {
        // title changes
        titleListener = (obs, oldVal, newVal) -> {
            task.setTitle(newVal);
            saveTasksToStorage();
        };
        txtFieldTaskName.textProperty().addListener(titleListener);

        // description changes
        descriptionListener = (obs, oldVal, newVal) -> {
            task.setDescription(newVal);
            renderMarkdown(newVal);
            saveTasksToStorage();
        };
        txtAreaDescription.textProperty().addListener(descriptionListener);

        // completion changes
        completionHandler = e -> {
            task.setComplete(checkBoxIsComplete.isSelected());
            saveTasksToStorage();
        };
        checkBoxIsComplete.setOnAction(completionHandler);
    }

    /**
     * clear the detail pane
     */
    private void clearTaskDetail() {
        cleanupEventListeners();
        currentDisplayedTask = null;
        txtFieldTaskName.clear();
        txtAreaDescription.clear();
        checkBoxIsComplete.setSelected(false);
        lblTaskInfo.setText("");
        if (webEngine != null) {
            webEngine.loadContent("", "text/html");
        }
        prioritySign.setImage(null);
        prioritySign.setVisible(false);
        prioritySign.setManaged(false);
    }
    /**
     * convert markdown to html and displayed them
     * @param markdownText string with markdown syntax
     */
    private void renderMarkdown(String markdownText) {
        if (markdownText == null) markdownText = "";

        Node document = markdownParser.parse(markdownText);
        String html = markdownRenderer.render(document);
        webEngine.loadContent(html, "text/html");
    }

    /**
     * save tasks to permanent storage through StorageManager
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
     * correspond priority to the corresponding UI component
     * @param task corresponding task object
     */
    private Image getPrioritySign(Task task) {
        double p = task.getPriority();
        final double EPS = 1e-6;

        try {
            if (Math.abs(p - 1.0) < EPS) {
                return new Image(Objects.requireNonNull(getClass()
                                .getResource("/com/example/planner/icon/priority_regular.png"))
                        .toExternalForm());
            } else if (Math.abs(p - 5.0) < EPS) {
                return new Image(Objects.requireNonNull(getClass()
                                .getResource("/com/example/planner/icon/priority_high.png"))
                        .toExternalForm());
            } else if (Math.abs(p - 2.5) < EPS) {
                return new Image(Objects.requireNonNull(getClass()
                                .getResource("/com/example/planner/icon/priority_medium.png"))
                        .toExternalForm());
            } else if (Math.abs(p - 0.0) < EPS) {
                return new Image(Objects.requireNonNull(getClass()
                                .getResource("/com/example/planner/icon/priority_low.png"))
                        .toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Error loading priority icon: " + e.getMessage());
        }
        return null;
    }

    /**
     * sync control from/based on year-month data
     */
    private void syncControlsFromYearMonth() {
        updatingControls = true;

        // month
        int monthIndex = currentYearMonth.getMonthValue() - 1; // 0-based index into cmbMonth
        if (monthIndex >= 0 && monthIndex < cmbMonth.getItems().size()) {
            cmbMonth.getSelectionModel().select(monthIndex);
        }

        // year
        if (spnYear.getValueFactory() != null) {
            spnYear.getValueFactory().setValue(currentYearMonth.getYear());
        }

        updatingControls = false;
    }

    /**
     * update calendar based on user selection of month and year
     */
    private void updateCalendarFromControls() {
        int selectedMonthIndex = cmbMonth.getSelectionModel().getSelectedIndex();
        if (selectedMonthIndex < 0) {
            selectedMonthIndex = currentYearMonth.getMonthValue() - 1;
        }

        int year = spnYear.getValue() != null ? spnYear.getValue() : currentYearMonth.getYear();
        Month month = Month.of(selectedMonthIndex + 1);

        currentYearMonth = YearMonth.of(year, month);
        buildCalendar();
    }



    @FXML
    private void onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        syncControlsFromYearMonth();
        buildCalendar();
    }

    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        syncControlsFromYearMonth();
        buildCalendar();
    }


    @FXML
    private void onNewTask() {


        masterController.openWindow("/com/example/planner/PopupSelection.fxml", "Add New Tasks", () -> {
                    // callback runs AFTER the popup is closed
                    // Reload task list from shared data
                    tasks = masterController.getSharedData("Tasks");
            syncControlsFromYearMonth();
            buildCalendar(); // refresh the UI
                }, null
        );
    }

    @FXML
    private void onDashboard() {

        masterController.closeWindow("Calendar");
        masterController.openWindow("/com/example/planner/Dashboard.fxml", "Dashboard", null, null);
    }

    @FXML
    private void onCalendar() {

        masterController.closeWindow("Calendar");
        masterController.openWindow("/com/example/planner/Calendar.fxml", "Calendar", null, null);

    }

    @FXML
    private void onSetting() {
        masterController.openWindow("/com/example/planner/Setting.fxml", "Setting", null, null);
    }

    @FXML
    private void onSearch() {

        masterController.openWindow("/com/example/planner/SearchView.fxml", "Search", null, null);
    }

    @FXML
    private void onHelp() {
        masterController.openWindow("/com/example/planner/Help.fxml", "Help",null,null);
    }
}
