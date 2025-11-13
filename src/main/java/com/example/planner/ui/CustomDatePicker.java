package com.example.planner.ui;

import com.example.planner.utility.Date2Letter;
import com.example.planner.utility.DateDisplayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomDatePicker extends DatePicker {

    public CustomDatePicker() {
        super();
        installFactory();
        //setConverter(new DateDisplayer(this));
    }

    private void installFactory() {
        setDayCellFactory(dp -> new DateCell() {
            private final StackPane badge = buildBadge();
            private final Tooltip tip = new Tooltip();

            {
                setPadding(new Insets(4));
                setGraphicTextGap(4);
            }

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setTooltip(null);
                setGraphic(null);
                getStyleClass().removeAll("letter-day");

                if (empty || date == null) return;

                char letter = Date2Letter.letterDate(date).charAt(0);

                ((Label) badge.getChildren().get(0)).setText(String.valueOf(letter));
                setGraphic(wrapWithCornerBadge(badge));

                tip.setText("Letter Day: " + letter);
                setTooltip(tip);

                getStyleClass().add("letter-day");

            }


            private StackPane buildBadge() {
                Label lbl = new Label();
                lbl.setMinSize(18, 18);
                lbl.setPrefSize(18, 18);
                lbl.setMaxSize(18, 18);
                lbl.setStyle("-fx-font-size:10; -fx-font-weight:700; -fx-text-fill:black;");
                return new StackPane(lbl);
            }

            private Node wrapWithCornerBadge(Node b) {
                StackPane wrapper = new StackPane();
                StackPane.setAlignment(b, Pos.TOP_RIGHT);
                StackPane.setMargin(b, new Insets(2, 2, 0, 0));
                wrapper.getChildren().add(b);
                return wrapper;
            }
        });
    }

    public char getLetterForDate(LocalDate date) {
        return Date2Letter.letterDate(date).charAt(0);
    }

    public String getLetterDateLabel(LocalDate date) {
        char letter = Date2Letter.letterDate(date).charAt(0);
        return (letter != '0') ? "Letter Day: " + letter : "";
    }



}
