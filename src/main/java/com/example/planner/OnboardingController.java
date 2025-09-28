package com.example.planner;

import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.utility.SettingManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalTime;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnboardingController {
    @FXML
    private TextField daysInCycle;

    @FXML
    private GridPane midGrid;
    @FXML
    private HBox letterDates;

    @FXML
    private VBox periodRow;

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

    private final Map<String, Section> assigned = new HashMap<>();

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        daysInCycle.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                numOfDays = Integer.parseInt(newVal);
                letterDates.getChildren().clear();
                letterDates.setSpacing(0);                  //don't set fixed sapcing
                letterDates.setFillHeight(true);
                letterDates.setAlignment(Pos.CENTER);    //center

                for (int i = 65; i < 65 + numOfDays; i++) {
                    char date = (char) i;
                    Label letter = new Label(String.valueOf(date));
                    letter.setMaxWidth(Double.MAX_VALUE);   // allow stretching
                    letter.setAlignment(Pos.CENTER);        // center text in its slot
                    HBox.setHgrow(letter, Priority.ALWAYS); // distribute evenly
                    letterDates.getChildren().add(letter);
                }
            } catch (NumberFormatException e) {
                letterDates.getChildren().clear();
                numOfDays = 0;
            }
            rebuildGrid();
        });
    }

    @FXML
    public void onAddPeriod() {
        Label periodLabel = new Label("" + periodCounter);
        periodLabel.setPrefWidth(20);

        TextField startTimeField = new TextField("08:00");
        startTimeField.setPromptText("Start");
        startTimeField.setPrefWidth(40);

        TextField endTimeField = new TextField("09:00");
        endTimeField.setPromptText("End");
        endTimeField.setPrefWidth(40);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-text-fill: red;");
        deleteBtn.setPrefWidth(20);

        HBox periodContainer = new HBox(5);
        periodContainer.setAlignment(Pos.CENTER_LEFT);
        periodContainer.setPadding(new Insets(5));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        periodContainer.getChildren().addAll(periodLabel, startTimeField, endTimeField, spacer, deleteBtn);
        periodRow.getChildren().add(periodContainer);

        deleteBtn.setOnAction(e -> {
            periodRow.getChildren().remove(periodContainer);
            refreshPeriodDisplay();
            rebuildGrid();
        });

        periodCounter++;
        rebuildGrid();
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
            assigned.entrySet().removeIf(en -> en.getValue() == section); // purge uses
            refreshAllCellMenus();
        });

        refreshAllCellMenus();
    }

    @FXML
   public void onContinue() throws Exception {
        Setting setting = new Setting(new ArrayList<Section>());
        List<Section> result = new ArrayList<Section>();

        for (Map.Entry<String, Section> entry : assigned.entrySet()) {
            String key = entry.getKey();
            Section uiSection = entry.getValue();

            String[] rc = key.split(",");
            int c = Integer.parseInt(rc[0]);
            int r = Integer.parseInt(rc[1]);

            String letter = ((Label) letterDates.getChildren().get(c)).getText();

            HBox row = (HBox) periodRow.getChildren().get(r);
            TextField startTF = (TextField) row.getChildren().get(1);
            TextField endTF   = (TextField) row.getChildren().get(2);

            LocalTime start = LocalTime.parse(startTF.getText().trim());
            LocalTime end   = LocalTime.parse(endTF.getText().trim());

            ArrayList<LocalTime> span = new ArrayList<LocalTime>(2);
            span.add(start);
            span.add(end);

            // find existing section by name
            Section built = null;
            for (int i = 0; i < result.size(); i++) {
                Section s = result.get(i);
                if (s.getName().equals(uiSection.getName())) {
                    built = s;
                    break;
                }
            }
            if (built == null) {
                built = new Section(
                        uiSection.getName(),
                        new ArrayList<String>(),
                        new ArrayList<ArrayList<LocalTime>>(),
                        uiSection.getColor()
                );
                result.add(built);
            }


            built.addTimeSlot(letter,span);
        }

        for (int i = 0; i < result.size(); i++) {
            setting.addSection(result.get(i));
        }

        SettingManager.save(setting);
        for(Section section:setting.getSections()){
        System.out.println(section.getLetterDates());}
        masterController.setSharedData("setting",setting);
        masterController.closeWindow("Welcome");
        masterController.openWindow("/com/example/planner/Dashboard.fxml","Dashboard",null);
    }





    //utility method for refreshing period
    private void refreshPeriodDisplay() {
        int i = 1;
        for (javafx.scene.Node node : periodRow.getChildren()) {
            if (node instanceof HBox hb) {
                if (!hb.getChildren().isEmpty() && hb.getChildren().get(0) instanceof Label lbl) {
                    lbl.setText("" + i);
                    i++;
                }
            }
        }

        periodCounter = i;
    }



    private void rebuildGrid() {
        int cols = numOfDays;
        int rows = periodRow.getChildren().size();

        midGrid.getChildren().clear();
        midGrid.getColumnConstraints().clear();
        midGrid.getRowConstraints().clear();

        if (cols <= 0 || rows <= 0) {return;}

        for (int c = 0; c < cols; c++) {
            midGrid.getColumnConstraints().add(new ColumnConstraints(90));
        }
        for (int r = 0; r < rows; r++) {
            midGrid.getRowConstraints().add(new RowConstraints(32));
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MenuButton cell = new MenuButton("Assign…");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cell.setStyle("-fx-border-color:#ddd;");
                cell.getItems().setAll(buildMenuItems(cell, c, r));

                Section s = assigned.get(key(c, r));
                if (s != null) apply(cell, s);

                midGrid.add(cell, c, r);
            }
        }
    }

    private List<MenuItem> buildMenuItems(MenuButton cell, int col, int row) {
        MenuItem clear = new MenuItem("— Clear —");
        clear.setOnAction(e -> {
            assigned.remove(key(col, row));
            cell.setText("Assign…");
            cell.setStyle("-fx-border-color:#ddd;");
            cell.setGraphic(null);
        });

        // one item per section
        List<MenuItem> items = new java.util.ArrayList<>();
        items.add(clear);
        for (Section s : sections) {
            MenuItem mi = new MenuItem(s.getName());
            mi.setOnAction(e2 -> {
                assigned.put(key(col, row), s);
                apply(cell, s);
            });
            items.add(mi);
        }
        return items;
    }

    private void apply(MenuButton cell, Section s) {
        cell.setText(s.getName());
        cell.setStyle("-fx-border-color:#ddd; -fx-background-color:" + s.getColor() + ";");
    }

    private String key(int c, int r) { return c + "," + r; }

    private void refreshAllCellMenus() {
        for (Node n : midGrid.getChildren()) {
            if (n instanceof MenuButton mb) {
                Integer c = GridPane.getColumnIndex(mb);
                if (c == null) {c = 0;}
                Integer r = GridPane.getRowIndex(mb);
                if (r == null) {r = 0;}
                mb.getItems().setAll(buildMenuItems(mb, c, r));
            }
        }
    }









}