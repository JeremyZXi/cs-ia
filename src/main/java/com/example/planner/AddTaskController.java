package com.example.planner;

import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.module.Task;
import com.example.planner.ui.CustomDatePicker;
import com.example.planner.utility.StorageManager;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.control.PopOver;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * window use to add new task
 * <p>
 * Add new tasks
 * process data & permanent storage
 */
public class AddTaskController {

    @FXML
    private ImageView priorityImage;


    @FXML
    private TitledPane sectionOption;


    @FXML
    private Label lblDueInfo;

    @FXML
    private Label lblTaskName;

    @FXML
    private Button timeSpanBtn;


    @FXML
    private TextArea txtAreaTaskDescription;

    @FXML
    private TextField txtFiledTaskName;

    @FXML
    private WebView wvTaskDescription;

    @FXML
    private VBox vboxLeft;

    private final Tooltip emptyFieldTooltip = new Tooltip("This field cannot be empty");
    private WebEngine webEngine;

    private MasterController masterController;
    private Map<String, Task> tasks = new HashMap<>();

    private Setting setting;

    private final CustomDatePicker datePicker = new CustomDatePicker();
    private Section selectedSection;
    private Integer selectedMinutes = null;
    private double priority = 1.0;

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        tasks = masterController.getSharedData("Tasks");
        setting = masterController.getSharedData("setting");
        Platform.runLater(() -> txtFiledTaskName.requestFocus());


        vboxLeft.getChildren().add(2, datePicker);
        // md syntx converter
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        webEngine = wvTaskDescription.getEngine();

        txtFiledTaskName.textProperty().addListener((obs, oldVal, newVal) -> lblTaskName.setText(newVal));
        txtAreaTaskDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            Node document = parser.parse(newVal);
            String html = renderer.render(document);
            webEngine.loadContent(html, "text/html");

        });

        // add Enter key event handler to the task name field
        txtFiledTaskName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    handleEnter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //listener for date picker to update the preview and update section selection
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblDueInfo.setText(newVal.toString() + " (" + datePicker.getLetterForDate(newVal) + " day)");
            ArrayList<Section> selected = sectionFilter(String.valueOf(datePicker.getLetterForDate(newVal)));
            VBox optionLists = new VBox(5);
            for (Section section : selected) {
                Button sectionBtn = new Button(section.getName());
                sectionBtn.setOnAction(e -> {
                    selectedSection = section;
                    lblDueInfo.setText(newVal + " (" + datePicker.getLetterForDate(newVal) + " day) " + section.getName());
                    sectionOption.setText(section.getName());
                });
                optionLists.getChildren().add(sectionBtn);
            }
            sectionOption.setContent(optionLists);

        });

        //instruct the user to filled missing field
        txtFiledTaskName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && txtFiledTaskName.getText().trim().isEmpty()) {
                Tooltip.install(txtFiledTaskName, emptyFieldTooltip);
            } else {
                Tooltip.uninstall(txtFiledTaskName, emptyFieldTooltip);
            }
        });

        //add time spinner
        Spinner<Integer> minutesSpinner = new Spinner<>();
        minutesSpinner.setEditable(true);
        minutesSpinner.setPrefWidth(100);
        minutesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 480, 60, 5)
        );

        // quick chips 4 usability
        Button b15 = new Button("15");
        Button b25 = new Button("25");
        Button b30 = new Button("30");
        Button b45 = new Button("45");
        Button b60 = new Button("60");
        Button b90 = new Button("90");

        for (Button b : new Button[]{b15, b25, b30, b45, b60, b90}) {
            b.getStyleClass().add("chip");
            b.setOnAction(e -> minutesSpinner.getValueFactory().setValue(Integer.parseInt(b.getText())));
        }

        // actions
        Button clearBtn = new Button("Clear");
        Button applyBtn = new Button("Apply");
        clearBtn.getStyleClass().add("secondary");
        applyBtn.getStyleClass().add("primary");

        Label title = new Label("Optional timespan (minutes)");
        title.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 6 0;");

        HBox quickRow = new HBox(6, b15, b25, b30, b45, b60, b90);
        HBox actions = new HBox(8, clearBtn, applyBtn);
        quickRow.setStyle("-fx-padding: 6 0 0 0;");
        actions.setStyle("-fx-padding: 10 0 0 0;");

        VBox content = new VBox(6, title, minutesSpinner, quickRow, actions);
        content.setStyle("-fx-padding: 10;");

        // PopOver
        PopOver timeSpanPop = new PopOver(content);
        timeSpanPop.setDetachable(false);
        timeSpanPop.setAutoHide(true);
        timeSpanPop.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);

        // reflect existing selection when opening
        timeSpanBtn.setOnAction(e -> {
            if (selectedMinutes != null) {
                minutesSpinner.getValueFactory().setValue(selectedMinutes);
            }
            timeSpanPop.show(timeSpanBtn);
        });

        // actions behavior
        applyBtn.setOnAction(e -> {
            selectedMinutes = minutesSpinner.getValue();
            // show the choice(min) on the button
            timeSpanBtn.setText(selectedMinutes + " min");
            timeSpanPop.hide();
        });

        clearBtn.setOnAction(e -> {
            selectedMinutes = null;
            timeSpanBtn.setText("Set timespan");
            timeSpanPop.hide();
        });


    }

    @FXML
    private void onHighPriority() {
        priority = 5.0;
        priorityImage.setImage(new Image(
                Objects.requireNonNull(getClass().getResource("/com/example/planner/icon/priority_high.png")).toExternalForm()
        ));
    }

    @FXML
    private void onMediumPriority() {
        priority = 2.5;
        priorityImage.setImage(new Image(
                Objects.requireNonNull(getClass().getResource("/com/example/planner/icon/priority_medium.png")).toExternalForm()
        ));
    }

    @FXML
    private void onLowPriority() {
        priority = 0.0;
        priorityImage.setImage(new Image(
                Objects.requireNonNull(getClass().getResource("/com/example/planner/icon/priority_low.png")).toExternalForm()
        ));
    }

    @FXML
    private void onRegularPriority() {
        priority = 1.0;
        priorityImage.setImage(new Image(
                getClass().getResource("/com/example/planner/icon/priority_regular.png").toExternalForm()
        ));
    }


    @FXML
    public void handleEnter() throws Exception {
        System.out.println(selectedMinutes);

        if (!txtFiledTaskName.getText().trim().isEmpty()) {
            //extract data from UI
            String title = txtFiledTaskName.getText();
            String description = txtAreaTaskDescription.getText();
            Section section = selectedSection;
            LocalDate date = datePicker.getValue();
            LocalTime start = LocalTime.now();
            LocalTime end = LocalTime.now();
            int timeSpan;
            if (selectedMinutes != null) {
                timeSpan = selectedMinutes;
            } else {
                timeSpan = 15;
            }

            //save data into task object
            Task task;
            if (date != null) {
                //task = new Task(date,datePicker.getLetterForDate(date),60,title,description);
                task = new Task(date, start, end, datePicker.getLetterForDate(date), timeSpan, title, description);
                if (selectedSection != null) {
                    task = new Task(section, date, datePicker.getLetterForDate(date), timeSpan, title, description);
                }
            } else {
                task = new Task(title, description);
            }
            task.setPriority(priority);
            tasks.put(task.getId(), task);
            masterController.setSharedData("Tasks", tasks);
            StorageManager.save(tasks);
            masterController.closeWindow("Add New Tasks");
            System.out.println("Closing");
        } else {
            txtFiledTaskName.requestFocus();
            txtFiledTaskName.setStyle("-fx-border-color: red; -fx-border-width: 1;");//set the boarder to red
        }
    }

    /**
     * filter the section of the day
     * @param letterDate letter date from A-H
     */
    public ArrayList<Section> sectionFilter(String letterDate) {
        ArrayList<Section> selected = new ArrayList<>();
        for (Section section : setting.getSections()) {
            for (String letter : section.getLetterDates()) {
                if (letter.equals(letterDate)) {
                    selected.add(section);
                    System.out.println(section.getName() + " filtered");
                }
            }
        }
        return selected;
    }
}
