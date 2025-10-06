package com.example.planner;

import com.example.planner.module.Setting;
import com.example.planner.utility.SettingManager;
import javafx.fxml.FXML;

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
        // Reuse parent's conversion + navigation, or customize
        masterController.closeWindow("Setting");
        onContinue();

    }
}
