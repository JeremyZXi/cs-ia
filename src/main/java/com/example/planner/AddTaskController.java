package com.example.planner;
import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.module.Task;
import com.example.planner.ui.CustomDatePicker;
import com.example.planner.utility.StorageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//for markdown
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.controlsfx.control.PopOver;


public class AddTaskController {

    @FXML
    private TitledPane sectionOption;

    @FXML
    private Button btnEnter;

    @FXML
    private Label lblDueInfo;

    @FXML
    private Label lblTaskName;

    @FXML
    private Spinner<?> spinPriority;

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

    private CustomDatePicker datePicker = new CustomDatePicker();
    private Section selectedSection;

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        tasks = masterController.getSharedData("Tasks");
        setting = masterController.getSharedData("setting");
        Platform.runLater(() -> txtFiledTaskName.requestFocus());




        vboxLeft.getChildren().add(2,datePicker);
        // md syntx converter
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        webEngine = wvTaskDescription.getEngine();

        txtFiledTaskName.textProperty().addListener((obs, oldVal, newVal) -> {
            lblTaskName.setText(newVal);
        });
        txtAreaTaskDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            Node document = parser.parse(newVal);
            String html = renderer.render(document);
            webEngine.loadContent(html,"text/html");

        });

        // Add Enter key event handler to the task name field
        txtFiledTaskName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    handleEnter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblDueInfo.setText(newVal.toString()+" ("+datePicker.getLetterForDate(newVal)+" day)");
            ArrayList<Section> selected = sectionFilter(String.valueOf(datePicker.getLetterForDate(newVal)));
            VBox optionLists = new VBox(5);
            for(Section section: selected){
                Button sectionBtn = new Button(section.getName());
                sectionBtn.setOnAction(e->{
                    selectedSection = section;
                    lblDueInfo.setText(newVal.toString()+" ("+datePicker.getLetterForDate(newVal)+" day) "+section.getName());
                    sectionOption.setText(section.getName());
                });
                optionLists.getChildren().add(sectionBtn);
            }
            sectionOption.setContent(optionLists);

        });

        txtFiledTaskName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && txtFiledTaskName.getText().trim().isEmpty()) {
                Tooltip.install(txtFiledTaskName, emptyFieldTooltip);
            } else {
                Tooltip.uninstall(txtFiledTaskName, emptyFieldTooltip);
            }
        });



    }


    @FXML
    public void handleEnter() throws Exception {

        if (!txtFiledTaskName.getText().trim().isEmpty()){
            String title = txtFiledTaskName.getText();
            String description = txtAreaTaskDescription.getText();
            Section section = selectedSection;
            LocalDate date = datePicker.getValue();
            LocalTime start = LocalTime.now();
            LocalTime end = LocalTime.now();

            Task task;
            if (date != null) {
                //task = new Task(date,datePicker.getLetterForDate(date),60,title,description);
                task = new Task(date,start,end, datePicker.getLetterForDate(date), 60,title,description);
                if (selectedSection!=null){
                    task = new Task(section,date,datePicker.getLetterForDate(date),60,title,description);
                }
            } else {
                task = new Task(title,description);
            }
            tasks.put(task.getId(), task);
            masterController.setSharedData("Tasks",tasks);
            StorageManager.save(tasks);
            masterController.closeWindow("Add New Tasks");
            System.out.println("Closing");
        } else{
            txtFiledTaskName.requestFocus();
            txtFiledTaskName.setStyle("-fx-border-color: red; -fx-border-width: 1;");//set the boarder to red
        }
    }

    public ArrayList<Section> sectionFilter(String letterDate){
        ArrayList<Section> selected = new ArrayList<>();
        for(Section section:setting.getSections()){
            for(String letter:section.getLetterDates()){
                if (letter.equals(letterDate)){
                    selected.add(section);
                    System.out.println(section.getName()+" filtered");
                }
            }
        }
        return selected;
    }
}
