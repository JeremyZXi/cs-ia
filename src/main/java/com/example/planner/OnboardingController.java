package com.example.planner;

import com.example.planner.module.Section;
import com.example.planner.module.Setting;
import com.example.planner.utility.Date2Letter;
import com.example.planner.utility.SettingManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A controller class responsible for the UI components of the Onboarding window, as well as some data processing
 * <p>
 * UI component handler i.e. button, text field etc.
 * Some data processing i.e. time, text, color
 */
public class OnboardingController {
    //UI components fxid
    @FXML
    protected TextField daysInCycle;
    @FXML
    protected GridPane midGrid;
    @FXML
    protected HBox letterDates;
    @FXML
    protected VBox periodRow;
    @FXML
    protected TextField sectionName;
    @FXML
    protected VBox sectionsList;
    @FXML
    protected ColorPicker tagColor;

    //share data and control windows
    protected MasterController masterController;

    //use to generate the table
    protected int numOfDays;
    protected int periodCounter = 1;

    // keep these protected so SettingController can reuse
    protected final ArrayList<Section> sections = new ArrayList<>();
    protected final Map<String, Section> assigned = new HashMap<>();

    private boolean suppressRebuild = false; // prevent rebuild loops when loading

    public void initialize() throws Exception {
        masterController = MasterController.getInstance();
        //listener to update the table based on user input
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

        setDays(8);

        periodRow.getChildren().clear();
        periodCounter = 1;

        // P1 08:00 - 09:15
        periodRow.getChildren().add(
                makePeriodRow(LocalTime.of(8, 0), LocalTime.of(9, 15))
        );
        periodCounter++;

        // P2 09:40 - 10:55
        periodRow.getChildren().add(
                makePeriodRow(LocalTime.of(9, 40), LocalTime.of(10, 55))
        );
        periodCounter++;

        // P3 11:10 - 12:25
        periodRow.getChildren().add(
                makePeriodRow(LocalTime.of(11, 10), LocalTime.of(12, 25))
        );
        periodCounter++;

        // P4 13:25 - 14:10 (01:25 PM - 02:10 PM)
        periodRow.getChildren().add(
                makePeriodRow(LocalTime.of(13, 25), LocalTime.of(14, 10))
        );
        periodCounter++;

        // P5 14:15 - 15:30 (02:15 PM - 03:30 PM)
        periodRow.getChildren().add(
                makePeriodRow(LocalTime.of(14, 15), LocalTime.of(15, 30))
        );
        periodCounter++;

        rebuildGrid();
    }

    /*UI syncing and control related methods*/

    /**
     * Generates letters A-Z based on the number of day
     * @param days number of day in the cycle
     */
    private void rebuildLetters(int days) {
        letterDates.getChildren().clear();
        letterDates.setSpacing(0);
        letterDates.setFillHeight(true);
        letterDates.setAlignment(Pos.CENTER);
        //based on ASCII
        for (int i = 65; i < 65 + days; i++) {
            char date = (char) i;
            Label letter = new Label(String.valueOf(date));
            letter.setMaxWidth(Double.MAX_VALUE);
            letter.setAlignment(Pos.CENTER);
            HBox.setHgrow(letter, Priority.ALWAYS);
            letterDates.getChildren().add(letter);
        }
    }
    /**
     *build a single period row with specific start/end times.
     * @param start the time which the period start
     * @param end the time which the period ends
     * @return a HBox contains label, text field, and delete button
     */
    protected HBox makePeriodRow(LocalTime start, LocalTime end) {
        Label periodLabel = new Label("" + periodCounter);
        periodLabel.setPrefWidth(20);


        // set the label "Start" +text field
        TextField startTimeField = new TextField(start.toString());
        startTimeField.setPromptText("Start");
        startTimeField.setPrefWidth(70);

        // set the label "End" + text field
        TextField endTimeField = new TextField(end.toString());
        endTimeField.setPromptText("End");
        endTimeField.setPrefWidth(70);

        // set delete button
        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-text-fill: red;");
        deleteBtn.setPrefWidth(28);

        // set container for period
        HBox periodContainer = new HBox(5);
        periodContainer.setAlignment(Pos.CENTER_LEFT);
        periodContainer.setPadding(new Insets(5));

        //spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        periodContainer.getChildren().addAll(periodLabel, startTimeField, endTimeField, spacer, deleteBtn);

        //event listener for the delete button
        deleteBtn.setOnAction(e -> {
            periodRow.getChildren().remove(periodContainer);
            refreshPeriodDisplay();
            rebuildGrid();
        });

        return periodContainer;
    }
    /**
     * This creates card containing section info in the sectionlist VBox
     * @param section an instance of Section
     */
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
    /**
     * Refresh the period displayed if deleted; Alter field periodCounter
     */
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

    /**
     * rebuild the table/grid displaying periods and days and add menu to it
     */
    protected void rebuildGrid() {
        int cols = numOfDays;
        int rows = periodRow.getChildren().size();

        //clear all grid
        midGrid.getChildren().clear();
        midGrid.getColumnConstraints().clear();
        midGrid.getRowConstraints().clear();

        //handle negative value
        if (cols <= 0 || rows <= 0) {
            return;
        }
        //add column
        for (int c = 0; c < cols; c++) {
            midGrid.getColumnConstraints().add(new ColumnConstraints(90));
        }
        //add row
        for (int r = 0; r < rows; r++) {
            midGrid.getRowConstraints().add(new RowConstraints(32));
        }
        //add menu to those cells
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

    /**
     * build menu items i.e. sections that user can select
     * @param cell the menu object that these items are in
     * @param col num of column
     * @param row num of row
     * @return items a list of menubutton
     */
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

    /**
     * assign the color of the selected section to the menu button
     * @param cell the cell containing the selection
     * @param s selected section
     */
    protected void apply(MenuButton cell, Section s) {
        cell.setText(s.getName());
        cell.setStyle("-fx-border-color:#ddd; -fx-background-color:" + s.getColor() + ";");
    }
    protected void refreshAllCellMenus() {
        for (Node n : midGrid.getChildren()) {
            if (n instanceof MenuButton mb) {
                Integer c = GridPane.getColumnIndex(mb);
                if (c == null) {
                    c = 0;
                }
                Integer r = GridPane.getRowIndex(mb);
                if (r == null) {
                    r = 0;
                }
                mb.getItems().setAll(buildMenuItems(mb, c, r));
            }
        }
    }

    /**
     * clear everything in editable UI
     */
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

    /*data processing method*/

    /**
     * utility method for data storage A.K.A. coordinate
     * @param c column
     * @param r row
     * @return String in the format of "column,row"
     */
    protected String key(int c, int r) {
        return c + "," + r;
    }

    /**
     * set day count without triggering rebuilds.
     * @param days the day you want to add
     */
    protected void setDays(int days) {
        suppressRebuild = true;
        this.numOfDays = days;
        daysInCycle.setText(Integer.toString(days)); // this also rebuilds letters
        rebuildLetters(days);
        suppressRebuild = false;
    }

    /**
     * Load an existing Setting into the UI.
     * @param setting an instance of Setting to load
     */
    protected void loadFromSetting(Setting setting) {
        if (setting == null || setting.getSections() == null) return;

        clearAll();

        // load Sections
        for (Section s : setting.getSections()) {
            Section copy = new Section(s.getName(), s.getColor());
            sections.add(copy);
            addSectionToUI(copy);
        }

        // determine/get days from used letters (A B C D...)
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

        // collect unique time spans (start,end) and create period rows
        record Span(LocalTime a, LocalTime b) {
        }
        Map<String, Integer> spanToRow = new LinkedHashMap<>();
        List<Span> uniqueSpans = new ArrayList<>();

        for (Section s : setting.getSections()) {
            List<ArrayList<LocalTime>> slots = s.getTimeSlots();
            if (slots == null) continue; //handle null in case of data loss
            for (ArrayList<LocalTime> slot : slots) {
                if (slot.size() < 2) continue; //handle in sufficient period
                LocalTime a = slot.get(0);
                LocalTime b = slot.get(1);
                String key = a + "→" + b;
                if (!spanToRow.containsKey(key)) {
                    uniqueSpans.add(new Span(a, b));
                    spanToRow.put(key, uniqueSpans.size() - 1);
                }
            }
        }

        // sort by start then end for UI
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

        // build grid now that rows/cols are known
        rebuildGrid();

        // fill assignments AKA sections
        // Section lookup by name
        Map<String, Section> byName = sections.stream()
                .collect(Collectors.toMap(Section::getName, s -> s));

        for (Section s : setting.getSections()) {
            Section uiSection = byName.get(s.getName());
            if (uiSection == null) continue; //handle null sections
            List<String> letters = s.getLetterDates();
            List<ArrayList<LocalTime>> slots = s.getTimeSlots();
            if (letters == null || slots == null) continue; //handle missing letters and times

            //letter day and time
            for (int i = 0; i < Math.min(letters.size(), slots.size()); i++) {
                String L = letters.get(i);
                if (L == null || L.isEmpty()) continue; //handle missing letter
                int col = Character.toUpperCase(L.charAt(0)) - 'A'; //convert ACSII
                ArrayList<LocalTime> slot = slots.get(i);
                if (slot.size() < 2) continue; //handle insufficient time slot
                String k = slot.get(0) + "→" + slot.get(1);
                Integer row = spanToRow.get(k);
                //assign
                if (col >= 0 && col < numOfDays && row != null) {
                    assigned.put(key(col, row), uiSection);
                }
            }
        }

        // rebuild one last time to add cell backgrounds
        rebuildGrid();
        refreshAllCellMenus();
    }

    /**
     * Builds a Setting object from the current UI state.
     * @return a populated Setting instance based on user input
     */
    protected Setting toSettingFromUI() {

        Setting setting = new Setting(new ArrayList<>());
        List<Section> assembledSections = new ArrayList<>();

        // assigned: Map<"column,row", Section>
        for (Map.Entry<String, Section> entry : assigned.entrySet()) {

            String gridKey = entry.getKey();
            Section uiSection = entry.getValue();

            // extract column and row indices from key "column,row"
            String[] parts = gridKey.split(",");
            int columnIndex = Integer.parseInt(parts[0]);
            int rowIndex = Integer.parseInt(parts[1]);

            // get the day letter from the UI e.g. "A", "B", "C"
            String dayLetter = ((Label) letterDates.getChildren().get(columnIndex)).getText();

            // get the corresponding row of start/end time fields
            HBox timeRow = (HBox) periodRow.getChildren().get(rowIndex);
            TextField startField = (TextField) timeRow.getChildren().get(1);
            TextField endField = (TextField) timeRow.getChildren().get(2);

            // vonvert text input to LocalTime
            LocalTime startTime = LocalTime.parse(startField.getText().trim());
            LocalTime endTime = LocalTime.parse(endField.getText().trim());

            // build the timeslot list (start + end)
            ArrayList<LocalTime> timeSpan = new ArrayList<>(2);
            timeSpan.add(startTime);
            timeSpan.add(endTime);

            // find existing Section with the same name
            Section builtSection = null;
            for (Section existing : assembledSections) {
                if (existing.getName().equals(uiSection.getName())) {
                    builtSection = existing;
                    break;
                }
            }

            // if not found, create a new Section container
            if (builtSection == null) {
                builtSection = new Section(
                        uiSection.getName(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        uiSection.getColor()
                );
                assembledSections.add(builtSection);
            }

            // add the timeslot to this Section
            builtSection.addTimeSlot(dayLetter, timeSpan);
        }

        // add all built sections to the Setting
        for (Section section : assembledSections) {
            setting.addSection(section);
        }

        return setting;
    }

    /*UI handler AJA button and stuff*/
    @FXML
    public void onAddPeriod() {
        HBox row = makePeriodRow(LocalTime.of(8, 0), LocalTime.of(9, 0));
        periodRow.getChildren().add(row);
        periodCounter++;
        rebuildGrid();
    }
    @FXML
    public void onAddSection() {
        Color selectedColor = tagColor.getValue();
        //formating hex color
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
    @FXML
    public void onImportDate(ActionEvent event) throws IOException {
        Window owner =((Node)event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Date Configuration");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV file", "*.csv")
        );


        File file = fileChooser.showOpenDialog(owner );

        Path relativeDir = Paths.get("data");
        Files.createDirectories(relativeDir);


        String name = file.getName();
        String ext = "";
        int dot = name.lastIndexOf(".");
        if (dot >= 0) ext = name.substring(dot);


        String newName = "calendar2" + ext;

        Path destination = relativeDir.resolve(newName);

        Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

    }




    @FXML
    public void onImportHolidays(ActionEvent event) throws IOException {
        Window owner = ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select holiday configurations");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV file", "*.csv")
        );

        File file = fileChooser.showOpenDialog(owner);

        if (file == null) {
            return; //if user canceled selection
        }

        // read csv and covert to set
        Set<LocalDate> holidayDates = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    holidayDates.add(LocalDate.parse(line)); // 默认 ISO 格式 yyyy-MM-dd
                }
            }
        }


        Date2Letter.generateCalendarCsv(
                LocalDate.of(LocalDate.now().getYear(), 9, 1),
                365,
                holidayDates
        );
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
        masterController.openWindow("/com/example/planner/Dashboard.fxml", "Dashboard", null, null);
    }
}
