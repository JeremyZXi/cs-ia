package com.example.planner;

import javafx.fxml.*;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
import java.util.*;
import java.io.*;

public class MasterController {
    private static MasterController instance;
    private Map<String, Object> sharedData = new HashMap<>();
    private Map<String, Stage> windows = new HashMap<>();

    // Private constructor prevents instantiation from other classes
    private MasterController(){
        //loaddata
    }
    // Thread-safe Singleton instance retrieval method
    public static synchronized MasterController getInstance() {
        if (instance == null) {
            instance = new MasterController();
        }
        return instance;
    }
    // Method to retrieve shared data
    @SuppressWarnings("unchecked")
    public <T> T getSharedData(String key) {
        return (T) sharedData.get(key);
    }

    // Method to open a new window
    public void openWindow(String fxmlPath, String title, Stage callerStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // return to caller when new window is closed
            if (callerStage != null) {
                stage.setOnHidden(e -> callerStage.show());
            }

            stage.show();

            // Store the window for future reference
            windows.put(title, stage);

        } catch (IOException e) {
            showAlert("FXML File Opening Error", "Please try again.");
            System.out.println(e);
            // If the previous window (callerStage) exists, show it again to avoid a blank screen
            if (callerStage != null) {
                callerStage.show(); // Fallback
            }
        }
    }

    public void showAlert(String alertTitle, String message) {
        // Create and display an alert dialog with the given title and message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(alertTitle);
        alert.setHeaderText(null); // No header text for simplicity
        alert.setContentText(message);
        alert.showAndWait(); // Wait for the user to acknowledge the alert
    }

    // Method to handle button click from FXML
    public void onHelloButtonClick() {
        showAlert("Hello", "Hello from MasterController!");
    }
}