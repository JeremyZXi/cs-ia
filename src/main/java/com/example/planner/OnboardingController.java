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
import javafx.scene.Node;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class OnboardingController {
    @FXML protected TextField daysInCycle;
    @FXML protected GridPane midGrid;
    @FXML protected HBox letterDates;
    @FXML protected VBox periodRow;
    @FXML protected TextField sectionName;
    @FXML protected VBox sectionsList;
    @FXML protected ColorPicker tagColor;

    protected MasterController masterController;
    protected int numOfDays;
    protected int periodCounter = 1;

    // keep these protected so SettingController can reuse
    protected final ArrayList<Section> sections = new ArrayList<>();
    protected final Map<String, Section> assigned = new HashMap<>();

    private boolean suppressRebuild = false; // prevent rebuild loops when loading

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        daysInCycle.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                numOfDays = Integer.parseInt(newVal);
                rebuildLetters(numOfDays);
            } catch (NumberFormatException e) {
                letterDates.getChildren().clear();
                numOfDays = 0;
            }
            if (!suppressRebuild) rebuildGrid();
        });
    }

    private void rebuildLetters(int days) {
        letterDates.getChildren().clear();
        letterDates.setSpacing(0);
        letterDates.setFillHeight(true);
        letterDates.setAlignment(Pos.CENTER);
        for (int i = 65; i < 65 + days; i++) {
            char date = (char) i;
            Label letter = new Label(String.valueOf(date));
            letter.setMaxWidth(Double.MAX_VALUE);
            letter.setAlignment(Pos.CENTER);
            HBox.setHgrow(letter, Priority.ALWAYS);
            letterDates.getChildren().add(letter);
        }
    }

    @FXML
    public void onAddPeriod() {
        HBox row = makePeriodRow(LocalTime.of(8,0), LocalTime.of(9,0));
        periodRow.getChildren().add(row);
        periodCounter++;
        rebuildGrid();
    }

    /** New: build a single period row with specific start/end times. */
    protected HBox makePeriodRow(LocalTime start, LocalTime end) {
        Label periodLabel = new Label("" + periodCounter);
        periodLabel.setPrefWidth(20);

        TextField startTimeField = new TextField(start.toString());
        startTimeField.setPromptText("Start");
        startTimeField.setPrefWidth(70);

        TextField endTimeField = new TextField(end.toString());
        endTimeField.setPromptText("End");
        endTimeField.setPrefWidth(70);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-text-fill: red;");
        deleteBtn.setPrefWidth(28);

        HBox periodContainer = new HBox(5);
        periodContainer.setAlignment(Pos.CENTER_LEFT);
        periodContainer.setPadding(new Insets(5));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        periodContainer.getChildren().addAll(periodLabel, startTimeField, endTimeField, spacer, deleteBtn);

        deleteBtn.setOnAction(e -> {
            periodRow.getChildren().remove(periodContainer);
            refreshPeriodDisplay();
            rebuildGrid();
        });

        return periodContainer;
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
        addSectionToUI(section);
        refreshAllCellMenus();
    }

    /** New: add a section and render its row in the UI (reused by loader). */
    protected void addSectionToUI(Section section) {
        String hexColor = section.getColor();
        // spacing and padding for each section row
        HBox displaySection = new HBox(10);
        displaySection.setPadding(new Insets(5));
        displaySection.setAlignment(Pos.CENTER_LEFT);

        Region colorBox = new Region();
        colorBox.setPrefSize(16, 16);
        colorBox.setStyle("-fx-background-color: " + hexColor + "; -fx-border-color: black; -fx-border-radius: 2;");

        Label nameLabel = new Label(section.getName());

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-text-fill: red;");

        displaySection.getChildren().addAll(colorBox, nameLabel, deleteBtn);
        sectionsList.getChildren().add(displaySection);

        deleteBtn.setOnAction(e -> {
            sectionsList.getChildren().remove(displaySection);
            sections.remove(section);
            assigned.entrySet().removeIf(en -> en.getValue() == section); // purge uses
            refreshAllCellMenus();
            rebuildGrid();
        });
    }

    @FXML
    public void onContinue() throws Exception {
        Setting setting = toSettingFromUI();
        SettingManager.save(setting);
        for (Section section : setting.getSections()) {
            System.out.println(section.getLetterDates());
        }
        masterController.setSharedData("setting", setting);
        System.out.println("setting saved");
        masterController.closeWindow("Welcome");
        masterController.openWindow("/com/example/planner/Dashboard.fxml", "Dashboard", null,null);
    }

    /** Extracted: convert current UI state into a Setting. */
    protected Setting toSettingFromUI() {
        Setting setting = new Setting(new ArrayList<>());
        List<Section> result = new ArrayList<>();

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

            ArrayList<LocalTime> span = new ArrayList<>(2);
            span.add(start);
            span.add(end);

            Section built = null;
            for (Section s : result) {
                if (s.getName().equals(uiSection.getName())) {
                    built = s;
                    break;
                }
            }
            if (built == null) {
                built = new Section(
                        uiSection.getName(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        uiSection.getColor()
                );
                result.add(built);
            }
            built.addTimeSlot(letter, span);
        }

        for (Section s : result) {
            setting.addSection(s);
        }
        return setting;
    }

    // ===== helper utilities =====

    protected void refreshPeriodDisplay() {
        int i = 1;
        for (Node node : periodRow.getChildren()) {
            if (node instanceof HBox hb) {
                if (!hb.getChildren().isEmpty() && hb.getChildren().get(0) instanceof Label lbl) {
                    lbl.setText("" + i);
                    i++;
                }
            }
        }
        periodCounter = i;
    }

    protected void rebuildGrid() {
        int cols = numOfDays;
        int rows = periodRow.getChildren().size();

        midGrid.getChildren().clear();
        midGrid.getColumnConstraints().clear();
        midGrid.getRowConstraints().clear();

        if (cols <= 0 || rows <= 0) { return; }

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

    protected List<MenuItem> buildMenuItems(MenuButton cell, int col, int row) {
        MenuItem clear = new MenuItem("— Clear —");
        clear.setOnAction(e -> {
            assigned.remove(key(col, row));
            cell.setText("Assign…");
            cell.setStyle("-fx-border-color:#ddd;");
            cell.setGraphic(null);
        });

        List<MenuItem> items = new ArrayList<>();
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

    protected void apply(MenuButton cell, Section s) {
        cell.setText(s.getName());
        cell.setStyle("-fx-border-color:#ddd; -fx-background-color:" + s.getColor() + ";");
    }

    protected String key(int c, int r) { return c + "," + r; }

    protected void refreshAllCellMenus() {
        for (Node n : midGrid.getChildren()) {
            if (n instanceof MenuButton mb) {
                Integer c = GridPane.getColumnIndex(mb);
                if (c == null) { c = 0; }
                Integer r = GridPane.getRowIndex(mb);
                if (r == null) { r = 0; }
                mb.getItems().setAll(buildMenuItems(mb, c, r));
            }
        }
    }

    /** New: clear everything in the editor UI. */
    protected void clearAll() {
        sections.clear();
        assigned.clear();
        sectionsList.getChildren().clear();
        periodRow.getChildren().clear();
        periodCounter = 1;
        midGrid.getChildren().clear();
        midGrid.getColumnConstraints().clear();
        midGrid.getRowConstraints().clear();
        letterDates.getChildren().clear();
        daysInCycle.clear();
    }

    /** New: set day count without triggering intermediate rebuilds. */
    protected void setDays(int days) {
        suppressRebuild = true;
        this.numOfDays = days;
        daysInCycle.setText(Integer.toString(days)); // this also rebuilds letters
        rebuildLetters(days);
        suppressRebuild = false;
    }

    /** New: Load an existing Setting into the UI. */
    protected void loadFromSetting(Setting setting) {
        if (setting == null || setting.getSections() == null) return;

        clearAll();

        // 1) Sections
        for (Section s : setting.getSections()) {
            Section copy = new Section(s.getName(), s.getColor());
            sections.add(copy);
            addSectionToUI(copy);
        }

        // 2) Determine days from used letters (A..)
        int maxLetterIndex = 0;
        for (Section s : setting.getSections()) {
            List<String> letters = s.getLetterDates();
            if (letters == null) continue;
            for (String L : letters) {
                if (L != null && !L.isEmpty()) {
                    int idx = Character.toUpperCase(L.charAt(0)) - 'A';
                    if (idx > maxLetterIndex) maxLetterIndex = idx;
                }
            }
        }
        setDays(maxLetterIndex + 1);

        // 3) Collect unique spans (start,end) and create period rows
        record Span(LocalTime a, LocalTime b) {}
        Map<String, Integer> spanToRow = new LinkedHashMap<>();
        List<Span> uniqueSpans = new ArrayList<>();

        for (Section s : setting.getSections()) {
            List<ArrayList<LocalTime>> slots = s.getTimeSlots();
            if (slots == null) continue;
            for (ArrayList<LocalTime> slot : slots) {
                if (slot.size() < 2) continue;
                LocalTime a = slot.get(0);
                LocalTime b = slot.get(1);
                String key = a + "→" + b;
                if (!spanToRow.containsKey(key)) {
                    uniqueSpans.add(new Span(a, b));
                    spanToRow.put(key, uniqueSpans.size() - 1);
                }
            }
        }

        // Sort by start then end for a stable UI
        uniqueSpans = uniqueSpans.stream()
                .sorted(Comparator.<Span, LocalTime>comparing(s -> s.a)
                        .thenComparing(s -> s.b))
                .collect(Collectors.toList());
        spanToRow.clear();
        for (int i = 0; i < uniqueSpans.size(); i++) {
            Span sp = uniqueSpans.get(i);
            periodCounter = i + 1; // so labels start at 1
            HBox row = makePeriodRow(sp.a, sp.b);
            periodRow.getChildren().add(row);
            spanToRow.put(sp.a + "→" + sp.b, i);
        }
        periodCounter = uniqueSpans.size() + 1;

        // 4) Build grid now that rows/cols are known
        rebuildGrid();

        // 5) Fill assignments
        // Section lookup by name (because we created copies)
        Map<String, Section> byName = sections.stream()
                .collect(Collectors.toMap(Section::getName, s -> s));

        for (Section s : setting.getSections()) {
            Section uiSection = byName.get(s.getName());
            if (uiSection == null) continue;
            List<String> letters = s.getLetterDates();
            List<ArrayList<LocalTime>> slots = s.getTimeSlots();
            if (letters == null || slots == null) continue;

            for (int i = 0; i < Math.min(letters.size(), slots.size()); i++) {
                String L = letters.get(i);
                if (L == null || L.isEmpty()) continue;
                int col = Character.toUpperCase(L.charAt(0)) - 'A';
                ArrayList<LocalTime> slot = slots.get(i);
                if (slot.size() < 2) continue;
                String k = slot.get(0) + "→" + slot.get(1);
                Integer row = spanToRow.get(k);
                if (col >= 0 && col < numOfDays && row != null) {
                    assigned.put(key(col, row), uiSection);
                }
            }
        }

        // 6) Rebuild one last time to paint cell backgrounds
        rebuildGrid();
        refreshAllCellMenus();
    }
}
