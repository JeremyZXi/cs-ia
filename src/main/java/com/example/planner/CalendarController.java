package com.example.planner;

import com.example.planner.module.Task;
import com.example.planner.ui.TaskCalendarCard;
import com.example.planner.utility.Date2Letter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CalendarController implements Initializable {

    // Root
    @FXML
    private BorderPane root;

    // Header controls
    @FXML
    private Button btnPrevMonth;
    @FXML
    private Button btnNextMonth;
    @FXML
    private Button btnToday;
    @FXML
    private Button addTaskBtn;

    @FXML
    private HBox topHbox;

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

    // State
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private boolean updatingControls = false;
    private MasterController masterController;
    private Map<String, Task> tasks = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            masterController = MasterController.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Safely get shared tasks
        Object shared = masterController.getSharedData("Tasks");
        if (shared instanceof Map<?, ?> map) {
            //noinspection unchecked
            tasks = (Map<String, Task>) map;
        } else {
            tasks = new HashMap<>();
        }

        currentYearMonth = YearMonth.now();

        setupMonthCombo();
        setupYearSpinner();

        // Initial sync of controls and calendar
        syncControlsFromYearMonth();
        buildCalendar();
    }

    /* ----------------- Setup ----------------- */

    private void setupMonthCombo() {
        cmbMonth.getItems().clear();
        for (Month m : Month.values()) {
            String raw = m.name().toLowerCase();
            String name = Character.toUpperCase(raw.charAt(0)) + raw.substring(1); // January, February...
            cmbMonth.getItems().add(name);
        }

        cmbMonth.setOnAction(e -> {
            if (updatingControls) return;
            updateCalendarFromControls();
        });
    }

    private void setupYearSpinner() {
        int currentYear = currentYearMonth.getYear();
        SpinnerValueFactory<Integer> yearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2500, currentYear);
        spnYear.setValueFactory(yearFactory);

        spnYear.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingControls) return;
            updateCalendarFromControls();
        });
    }

    /* ----------------- Calendar building ----------------- */

    private void buildCalendar() {
        gridMonth.getChildren().clear();

        YearMonth ym = currentYearMonth;
        LocalDate firstOfMonth = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();

        // Java time: Monday=1 ... Sunday=7
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1-7
        int firstColumn = firstDayOfWeek - 1; // 0-6 (Mon-Sun)

        LocalDate today = LocalDate.now();

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

    /** Create a basic day cell with the day label and click handling. */
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

        // Click on the empty cell area selects this date (for "New Task" etc.)
        box.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectedDate = date;
                lblTaskInfo.setText("Selected date: " + Date2Letter.letterDate(date));
            }
        });

        return box;
    }

    /** Add TaskCalendarCard components to a given day cell for all tasks due on that date. */
    private void populateTasksForDay(VBox dayCell, LocalDate date) {
        if (tasks == null || tasks.isEmpty()) return;

        for (Task task : tasks.values()) {
            if (task == null || task.getDueDate() == null) continue;

            // ✅ compare against the cell date, NOT "today"
            if (date.equals(task.getDueDate())) {
                TaskCalendarCard card = new TaskCalendarCard(task, this::onTaskClicked);
                // you can set colors here if you like:
                // card.setAccentColor(Color.LIGHTGREEN);
                dayCell.getChildren().add(card);
            }
        }
    }

    /* ----------------- Task click handling ----------------- */

    /** Called when a TaskCalendarCard is clicked. */
    private void onTaskClicked(Task task) {
        if (task == null) return;
        selectedDate = task.getDueDate();
        showTaskInDetailsPane(task);
    }

    /** Update the right-hand detail pane with task info. */
    private void showTaskInDetailsPane(Task task) {
        txtFieldTaskName.setText(task.getTitle() != null ? task.getTitle() : "");
        txtAreaDescription.setText(task.getDescription() != null ? task.getDescription() : "");
        checkBoxIsComplete.setSelected(task.isComplete());

        if (task.getDueDate() != null) {
            lblTaskInfo.setText("Due: " + Date2Letter.letterDate(task.getDueDate()));
        } else {
            lblTaskInfo.setText("No due date");
        }

        if (wvDescription != null) {
            StringBuilder html = new StringBuilder("<html><body>");
            html.append("<h3>")
                    .append(task.getTitle() != null ? task.getTitle() : "(no title)")
                    .append("</h3>");
            if (task.getDescription() != null && !task.getDescription().isBlank()) {
                html.append("<p>").append(task.getDescription()).append("</p>");
            } else {
                html.append("<p>No extra notes.</p>");
            }
            if (task.getDueDate() != null) {
                html.append("<p><b>Due:</b> ").append(Date2Letter.letterDate(task.getDueDate())).append("</p>");
            }
            html.append("</body></html>");
            wvDescription.getEngine().loadContent(html.toString());
        }
    }

    /* ----------------- Sync between controls & state ----------------- */

    private void syncControlsFromYearMonth() {
        updatingControls = true;

        // Month
        int monthIndex = currentYearMonth.getMonthValue() - 1; // 0-based index into cmbMonth
        if (monthIndex >= 0 && monthIndex < cmbMonth.getItems().size()) {
            cmbMonth.getSelectionModel().select(monthIndex);
        }

        // Year
        if (spnYear.getValueFactory() != null) {
            spnYear.getValueFactory().setValue(currentYearMonth.getYear());
        }

        updatingControls = false;
    }

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

    /* ----------------- FXML event handlers ----------------- */

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
    private void onToday() {
        currentYearMonth = YearMonth.now();
        syncControlsFromYearMonth();
        buildCalendar();
        selectedDate = LocalDate.now();
        lblTaskInfo.setText("Today: " + Date2Letter.letterDate(selectedDate));
    }

    @FXML
    private void onNewTask() {
        // Default to today if no date is selected
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }

        txtFieldTaskName.clear();
        txtAreaDescription.clear();
        checkBoxIsComplete.setSelected(false);
        lblTaskInfo.setText("New task on " + Date2Letter.letterDate(selectedDate));

        if (wvDescription != null) {
            String html = "<html><body><h3>New task</h3><p>Date: "
                    + Date2Letter.letterDate(selectedDate) + "</p></body></html>";
            wvDescription.getEngine().loadContent(html);
        }
    }

    @FXML
    private void onDashboard() {
        System.out.println("Dashboard clicked");
    }

    @FXML
    private void onCalendar() {
        System.out.println("Calendar clicked");
    }

    @FXML
    private void onSetting() {
        System.out.println("Settings clicked");
    }

    @FXML
    private void onSearch() {
        System.out.println("Search clicked");
    }

    @FXML
    private void onHelp() {
        System.out.println("Help clicked");
    }
}
