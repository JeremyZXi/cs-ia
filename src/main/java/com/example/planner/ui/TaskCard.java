package com.example.planner.ui;

import com.example.planner.module.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


public class TaskCard extends HBox {

    private static TaskCard currentlySelectedCard = null; // tracks the selected card
    private final Consumer<Task> onSelectCallback;

    private final CheckBox checkBox;
    private final Label label;
    private Task task;
    private boolean isSelected = false;

    private final String defaultStyle = "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;";
    private final String selectedStyle = "-fx-background-color: #d0e8ff; -fx-border-color: #2196F3; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;";
    private final String completedStyle = "-fx-background-color: #e5e5e5; -fx-border-color: #cccccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;";

    public TaskCard(Task task,Consumer<Task> onSelectCallback) {
        this.task = task;
        this.onSelectCallback = onSelectCallback;

        // init components
        checkBox = new CheckBox(task.getTitle());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        label = new Label(task.getDueDate() != null ? task.getDueDate().format(formatter) : "No due date");

        checkBox.setPrefWidth(338);
        checkBox.setPrefHeight(50);
        checkBox.setPadding(new Insets(8, 8, 8, 8));
        checkBox.setSelected(task.isComplete());

        label.setTextFill(Color.web("#1888ed"));
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setMinWidth(50);

        setPrefWidth(250);
        setMinWidth(250);
        setPrefHeight(40);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setStyle(defaultStyle);

        getChildren().addAll(checkBox, label);

        setupListeners();
    }

    private void setupListeners() {
        checkBox.setOnAction(e -> {
            boolean isComplete = checkBox.isSelected();
            task.setComplete(isComplete);
            playCheckSound();
            if (isComplete) {
                this.setStyle(completedStyle);
                checkBox.setStyle("-fx-text-fill: #888888; -fx-strikethrough: true;");
            } else {
                this.setStyle(isSelected ? selectedStyle : defaultStyle);
                checkBox.setStyle("");
            }

            playCheckSound();
        });


        // select this card on click
        this.setOnMouseClicked(e -> select());
    }
    public static TaskCard getCurrentlySelectedTask() {
        if(currentlySelectedCard != null){
            return  currentlySelectedCard;
        }else {
            return  null;
        }

    }


    public void select() {
        if (currentlySelectedCard != null && currentlySelectedCard != this) {
            currentlySelectedCard.deselect();
        }

        isSelected = true;
        setStyle(selectedStyle);
        currentlySelectedCard = this;

        if (onSelectCallback != null) {
            onSelectCallback.accept(task);
        }
    }

    public void deselect() {
        isSelected = false;
        setStyle(defaultStyle);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
        checkBox.setText(task.getTitle());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        label.setText(task.getDueDate() != null ? task.getDueDate().format(formatter) : "No due date");
        checkBox.setSelected(task.isComplete());
    }
    private void playCheckSound() {
        try {
            String soundPath = getClass().getResource("/com/example/planner/ding-402325.mp3").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Could not play sound: " + e.getMessage());
        }
    }

}
