package com.example.planner;

import com.example.planner.module.Task;
import com.example.planner.utility.StorageManager;
import javafx.fxml.*;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
import java.util.*;
import java.io.*;
import java.io.StringWriter;
import java.io.PrintWriter;

public class MasterController {
    private static MasterController instance;
    private Map<String, Object> sharedData = new HashMap<>();
    private Map<String, Stage> windows = new HashMap<>();

    // Private constructor prevents instantiation from other classes
    private MasterController() throws Exception {
        //loaddata
        loadTasks();
    }
    private void loadTasks() throws Exception {
        Map<String, Task> tasks = new HashMap<>();
        if(StorageManager.storageExists()){
            tasks = StorageManager.load();
        } else{
            tasks = new HashMap<>();
        }
        setSharedData("Tasks",tasks);
    }
    // Thread-safe Singleton instance retrieval method
    public static synchronized MasterController getInstance() throws Exception {
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
            showAlert("FXML File Opening Error", "Error: " + e.getMessage() + "\n\nFull stack trace:\n" + getStackTraceString(e));
            System.out.println(e);
            e.printStackTrace();
            // If the previous window (callerStage) exists, show it again to avoid a blank screen
            if (callerStage != null) {
                callerStage.show(); // Fallback
            }
        } catch (Exception e) {
            showAlert("Unexpected Error", "Error: " + e.getMessage() + "\n\nFull stack trace:\n" + getStackTraceString(e));
            System.out.println(e);
            e.printStackTrace();
            // If the previous window (callerStage) exists, show it again to avoid a blank screen
            if (callerStage != null) {
                callerStage.show(); // Fallback
            }
        }
    }
    public void hideWindow(String title) {
        Stage stage = windows.get(title);
        if (stage != null) {
            stage.hide();
        }
    }

    public void showWindow(String title) {
        Stage stage = windows.get(title);
        if (stage != null) {
            stage.show();
        }
    }

    public void closeWindow(String title) {
        Stage stage = windows.get(title);
        if (stage != null) {
            stage.close();
            windows.remove(title);
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

    // Method to add shared data
    public void setSharedData(String key, Object value) {
        sharedData.put(key, value);
    }

    // Helper method to get stack trace as string
    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }


}