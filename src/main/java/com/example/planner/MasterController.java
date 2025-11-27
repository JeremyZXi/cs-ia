package com.example.planner;

import com.example.planner.module.Setting;
import com.example.planner.module.Task;
import com.example.planner.utility.SettingManager;
import com.example.planner.utility.StorageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class MasterController {
    private static MasterController instance;
    private final Map<String, Object> sharedData = new HashMap<>();
    private final Map<String, Stage> windows = new HashMap<>();

    // Private constructor prevents instantiation from other classes
    private MasterController() throws Exception {
        //loaddata
        loadTasks();
        loadSetting();
    }

    /**
     * use to load tasks from permanent storage
     * @throws Exception
     */
    private void loadTasks() throws Exception {
        Map<String, Task> tasks = new HashMap<>();
        if (StorageManager.storageExists()) {
            tasks = StorageManager.load();
        } else {
            tasks = new HashMap<>();
        }
        setSharedData("Tasks", tasks);
    }

    /**
     * get window instance
     * @param key key of the window
     * @return Window
     */
    public Stage getWindows(String key) {
        System.out.println(windows.get(key));
        return windows.get(key);
    }

    /**
     * load setting from permanent storage
     * @throws Exception
     */
    private void loadSetting() throws Exception {
        Setting setting = new Setting();
        if (SettingManager.storageExists()) {
            setting = SettingManager.load();
        }
        setSharedData("setting", setting);
    }

    /**
     *
     * @return instance of MasterController
     * @throws Exception
     */
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

    /**
     * Open a new window
     * @param fxmlPath path to the FXML
     * @param title title of the window
     * @param onCloseCallback callback method
     * @param callerStage the stage object that calls this window
     */
    public void openWindow(String fxmlPath, String title, Runnable onCloseCallback, Stage callerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> {
                windows.remove(title); // Remove from map when window is closed
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }
            });
            if (callerStage != null) {
                stage.setOnHidden(e -> callerStage.show());
            }


            // Store the stage in the windows map
            windows.put(title, stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * hide the window with that title
     * @param title hide that window
     */
    public void hideWindow(String title) {
        Stage stage = windows.get(title);
        if (stage != null) {
            stage.hide();
        }
    }

    /**
     * show the window with that title
     * @param title title/key
     */
    public void showWindow(String title) {
        Stage stage = windows.get(title);
        if (stage != null) {
            stage.show();
        }
    }

    /**
     *show the window with that title
     * @param title title/key
     */
    public void closeWindow(String title) {
        Stage stage = windows.get(title);
        if (stage != null) {
            stage.close();
            windows.remove(title);
        }
    }

    /**
     * show alert pop up
     * @param alertTitle title of that popup
     * @param message message in the alert
     */
    public void showAlert(String alertTitle, String message) {
        // Create and display an alert dialog with the given title and message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(alertTitle);
        alert.setHeaderText(null); // No header text for simplicity
        alert.setContentText(message);
        alert.showAndWait(); // Wait for the user to acknowledge the alert
    }


    /**
     * set shared data into master controller
     * @param key key of that object
     * @param value the object itself
     */
    public void setSharedData(String key, Object value) {
        sharedData.put(key, value);
    }

    /**
     * method to get stack trace string
     * @param e
     * @return string of stack tace
     */
    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }


}