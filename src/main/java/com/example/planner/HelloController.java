package com.example.planner;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button helloButton;

    private MasterController masterController;

    @FXML
    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
    }

    @FXML
    private void onHelloButtonClick() {
        masterController.closeWindow("Login");



        masterController.openWindow("/com/example/planner/Dashboard.fxml", "Dashboard", null);
    }





}
