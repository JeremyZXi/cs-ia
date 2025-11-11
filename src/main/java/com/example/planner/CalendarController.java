package com.example.planner;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;



public class CalendarController {
    @FXML
    private Button addTaskBtn;
    private MasterController masterController;

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();

    }




    //navbar navigation
    @FXML
    public void onSetting(){
        Stage currentStage = (Stage) addTaskBtn.getScene().getWindow();
        masterController.closeWindow("Calendar");
        masterController.openWindow("/com/example/planner/Setting.fxml", "Setting", null,currentStage);
    }

    @FXML
    public void onCalendar(){
        Stage currentStage = (Stage) addTaskBtn.getScene().getWindow();
        masterController.closeWindow("Calendar");
        masterController.openWindow("/com/example/planner/Calendar.fxml", "Calendar", null,null);
    }

    @FXML
    public void onDashboard(){
        Stage currentStage = (Stage) addTaskBtn.getScene().getWindow();
        masterController.closeWindow("Calendar");
        masterController.openWindow("/com/example/planner/Dashboard.fxml", "Dashboard", null,null);
    }
}
