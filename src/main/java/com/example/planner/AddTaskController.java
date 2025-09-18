package com.example.planner;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
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

    public void initialize(){

    }
}
