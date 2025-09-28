package com.example.planner;

import javafx.application.Application;
import javafx.stage.Stage;

public class AppLauncher extends Application {
    /** NEW **/
    private MasterController masterController;
    @Override
    public void start(Stage stage) throws Exception
    {
        /** NEW **/
        masterController = MasterController.getInstance();
       // masterController.openWindow("/com/example/planner/hello-view.fxml", "Login", null);
        masterController.openWindow("/com/example/planner/Onboarding.fxml","Set up your day", null);
    }

    public static void main(String[] args){
        launch(args);
    }
}