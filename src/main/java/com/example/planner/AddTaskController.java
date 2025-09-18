package com.example.planner;
import com.example.planner.module.Task;
import com.example.planner.utility.StorageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

    private MasterController masterController;
    private Map<String, Task> tasks = new HashMap<>();

    public void initialize(){
        masterController = MasterController.getInstance();
        tasks = masterController.getSharedData("Tasks");
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
    }
}
