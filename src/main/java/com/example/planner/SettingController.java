package com.example.planner;

import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.utility.SettingManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * A controller class responsible for the UI components of the Setting window, as well as some data processing
 * <p>
 * Inherited from OnboardingController
 * See OnboardingController Class
 */
public class SettingController extends OnboardingController {

    private static final Path DIR = Path.of(System.getProperty("user.dir"), "data");

    @Override
    public void initialize() throws Exception {
        super.initialize();


        // load existing setting
        Setting setting = SettingManager.load();
        loadFromSetting(setting);
    }


    @FXML
    public void onSave() throws Exception {
        // Reuse parent's conversion + navigation
        masterController.closeWindow("Setting");
        Setting setting = toSettingFromUI();
        SettingManager.save(setting);
        for (Section section : setting.getSections()) {
            System.out.println(section.getLetterDates());
        }
        masterController.setSharedData("setting", setting);
        System.out.println("setting saved");
    }
    @FXML public void onExport() throws Exception{
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder to Save Files");
        File selectedDirectory = directoryChooser.showDialog(daysInCycle.getScene().getWindow());

        File setingContent = new File(String.valueOf(DIR.resolve("setting.json")));
        File storageContent = new File(String.valueOf(DIR.resolve("storage.json")));

        if (selectedDirectory == null) {
            System.out.println("No directory selected.");
            return; // user canceled
        }
        try {
            File setting = new File(selectedDirectory, "setting.json");
            File storage = new File(selectedDirectory, "storage.json");
            try (FileWriter writer = new FileWriter(setting)) {
                writer.write(Files.readString(setingContent.toPath()));
            }
            try (FileWriter writer = new FileWriter(storage)) {
                writer.write(Files.readString(storageContent.toPath()));
            }
            System.out.println("Files exported to: " + selectedDirectory.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }

        }


    @FXML
    public void onImport() throws Exception {
        Window owner = daysInCycle.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Data");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON file", "*.json")
        );

        File file = fileChooser.showOpenDialog(owner);

        if (file == null) {
            return; // user cancelled
        }

        // Create /data directory if it doesn't exist
        Path targetDir = Paths.get("data");
        Files.createDirectories(targetDir);

        // Keep the original filename
        Path destination = targetDir.resolve(file.getName());

        // Copy the file
        Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("File copied to: " + destination.toAbsolutePath());
    }




}




