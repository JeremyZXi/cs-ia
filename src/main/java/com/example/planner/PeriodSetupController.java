package com.example.planner;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PeriodSetupController {

    @FXML private VBox periodRowsContainer;

    private final List<PeriodRow> periodRows = new ArrayList<>();

    private int periodCounter = 1;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

    @FXML
    public void initialize() {
        onAddPeriod(); // Start with one period
    }

    @FXML
    public void onAddPeriod() {
        Label periodLabel = new Label("Period " + periodCounter);

        TextField startTimeField = new TextField("08:00");
        startTimeField.setPromptText("Start (e.g., 8:00)");

        TextField endTimeField = new TextField("09:00");
        endTimeField.setPromptText("End (e.g., 9:15)");

        Button deleteBtn = new Button("Delete");

        HBox row = new HBox(10, periodLabel, new Label("From:"), startTimeField, new Label("To:"), endTimeField, deleteBtn);
        PeriodRow periodRow = new PeriodRow("Period " + periodCounter, startTimeField, endTimeField, row);
        periodRows.add(periodRow);
        periodRowsContainer.getChildren().add(row);

        deleteBtn.setOnAction(e -> {
            periodRows.remove(periodRow);
            periodRowsContainer.getChildren().remove(row);
            refreshPeriodLabels();
        });

        periodCounter++;
    }

    @FXML
    public void onContinue() {
        List<Period> result = new ArrayList<>();

        for (PeriodRow pr : periodRows) {
            String startStr = pr.startTimeField.getText().trim();
            String endStr = pr.endTimeField.getText().trim();
            LocalTime startTime, endTime;

            try {
                startTime = LocalTime.parse(startStr, timeFormatter);
                endTime = LocalTime.parse(endStr, timeFormatter);
            } catch (DateTimeParseException e) {
                showAlert("Invalid time format in " + pr.name + ". Please use HH:mm (e.g., 8:00 or 14:30).");
                return;
            }

            if (!startTime.isBefore(endTime)) {
                showAlert(pr.name + ": Start time must be before end time.");
                return;
            }

            result.add(new Period(pr.name, startTime, endTime));
        }


        System.out.println("Defined periods:");
        for (Period p : result) {
            System.out.println(p.name + ": " + p.start + " - " + p.end);
        }
        

        // TODO: Pass result to next screen or persist
    }

    private void refreshPeriodLabels() {
        int i = 1;
        for (PeriodRow pr : periodRows) {
            pr.name = "Period " + i;
            ((Label) pr.container.getChildren().get(0)).setText(pr.name);
            i++;
        }
        periodCounter = i;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static class PeriodRow {
        String name;
        TextField startTimeField;
        TextField endTimeField;
        HBox container;

        public PeriodRow(String name, TextField startTimeField, TextField endTimeField, HBox container) {
            this.name = name;
            this.startTimeField = startTimeField;
            this.endTimeField = endTimeField;
            this.container = container;
        }
    }

    public static class Period {
        String name;
        LocalTime start;
        LocalTime end;

        public Period(String name, LocalTime start, LocalTime end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }
}
