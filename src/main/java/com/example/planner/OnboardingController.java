package com.example.planner;

import com.example.planner.module.Section;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class OnboardingController {
    @FXML
    private TextField daysInCycle;

    @FXML
    private TextField sectionName;

    @FXML
    private VBox sectionsList;

    @FXML
    private ColorPicker tagColor;

    MasterController masterController;
    private int numOfDays;

    private int periodCounter = 1;

    private ArrayList<Section> sections = new ArrayList<>();

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        daysInCycle.textProperty().addListener((oldVal,newVal,obs)->{
            numOfDays = Integer.parseInt(newVal);
        });

    }
    public void onAddPeriod(){
        Label periodLabel = new Label("Period " + periodCounter);

        TextField startTimeField = new TextField("08:00");
        startTimeField.setPromptText("Start (e.g., 8:00)");

        TextField endTimeField = new TextField("09:00");
        endTimeField.setPromptText("End (e.g., 9:15)");

        Button deleteBtn = new Button("Delete");

        periodCounter++;
    }

    @FXML
    public void onAddSection() {
        Color selectedColor = tagColor.getValue();
        String hexColor = String.format("#%02X%02X%02X%02X",
                (int) (selectedColor.getRed() * 255),
                (int) (selectedColor.getGreen() * 255),
                (int) (selectedColor.getBlue() * 255),
                (int) (selectedColor.getOpacity() * 255));

        String name = sectionName.getText();
        Section section = new Section(name, hexColor);
        sections.add(section);

        //spacing and padding for each section row
        HBox displaySection = new HBox(10);
        displaySection.setPadding(new Insets(5));
        displaySection.setAlignment(Pos.CENTER_LEFT);

        //use to preview color
        Region colorBox = new Region();
        colorBox.setPrefSize(16, 16);
        colorBox.setStyle("-fx-background-color: " + hexColor + "; -fx-border-color: black; -fx-border-radius: 2;");

        Label nameLabel = new Label(name);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-text-fill: red;");

        displaySection.getChildren().addAll(colorBox, nameLabel, deleteBtn);

        sectionsList.getChildren().add(displaySection);

        deleteBtn.setOnAction(e -> {
            sectionsList.getChildren().remove(displaySection);
            sections.remove(section);
        });
    }


}
