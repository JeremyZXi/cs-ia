package com.example.planner;

import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.utility.SettingManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SettingController extends OnboardingController {

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


}
