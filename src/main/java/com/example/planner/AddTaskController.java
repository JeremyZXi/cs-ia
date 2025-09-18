package com.example.planner;
import com.example.planner.module.Task;
import com.example.planner.utility.StorageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
//for markdown
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;


public class AddTaskController {
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

    private WebEngine webEngine;

    private MasterController masterController;
    private Map<String, Task> tasks = new HashMap<>();

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        tasks = masterController.getSharedData("Tasks");
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
    }

    @FXML
    public void handleEnter() throws Exception {
        String title = txtFiledTaskName.getText();
        String description = txtAreaTaskDescription.getText();
        Task task = new Task(LocalDate.now(),60,title,description);
        tasks.put(task.getId(), task);
        masterController.setSharedData("Tasks",tasks);
        StorageManager.save(tasks);
        masterController.closeWindow("Add New Tasks");
        System.out.println("Closing");
    }
}
