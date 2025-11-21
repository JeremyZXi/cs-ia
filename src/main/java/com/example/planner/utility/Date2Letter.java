package com.example.planner.utility;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
/**
 * A utility class converting calendar date to letter date
 * <p>
 * LocalDate to A-H
 */
public class Date2Letter {

    // default calendar file
    private static final Path DEFAULT_CALENDAR_CSV = Paths.get("data", "calendar.csv");

    // 8-day cycle letters
    private static final String[] CYCLE_LETTERS = {"A", "B", "C", "D", "E", "F", "G", "H"};

    // loaded calendar data (date, weekday, cycle_letter)
    private static List<String[]> data = readCSV(DEFAULT_CALENDAR_CSV.toString());

    /**
     * Look up the cycle letter for a given LocalDate, based on calendar.csv
     * returns '0' if not found.
     * @param d LocalDate object
     * @return letter the corresponding letter date
     */
    public static String letterDate(LocalDate d) {
        if (data == null) {
            return "0";
        }
        String letter = "0";
        for (String[] row : data) {
            // row[0] = date (YYYY-MM-DD), row[2] = cycle_letter
            if (row[0].equals(d.toString())) {
                letter = String.valueOf(row[2].charAt(0));
                break;
            }
        }
        return letter;
    }

    /**
     * generate the calendar CSV
     * columns: date, weekday, cycle_letter
     * - cycle_letter advances through A..H on weekdays (Mon–Fri)
     * - weekends (Sat, Sun) get '0' and do NOT advance the cycle
     * - holidays get '0' and do NOT advance the cycle
     * @param startDate start date
     * @param holidays list of holidays
     * @param numDays number of days to generate
     * @param outputPath path
     */
    public static void generateCalendarCsv(
            LocalDate startDate,
            int numDays,
            Set<LocalDate> holidays,
            Path outputPath
    ) throws IOException {

        // Ensure parent directory exists
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        int currentCycleIndex = 0;

        try (Writer writer = Files.newBufferedWriter(outputPath);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Header row
            csvWriter.writeNext(new String[]{"date", "weekday", "cycle_letter"});

            for (int offset = 0; offset < numDays; offset++) {
                LocalDate currentDate = startDate.plusDays(offset);
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

                boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
                boolean isHoliday = holidays != null && holidays.contains(currentDate);

                String cycleLetter;
                if (isWeekend || isHoliday) {
                    cycleLetter = "0";
                } else {
                    cycleLetter = CYCLE_LETTERS[currentCycleIndex];
                    currentCycleIndex = (currentCycleIndex + 1) % CYCLE_LETTERS.length;
                }

                String weekdayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH); // e.g. "Monday"

                csvWriter.writeNext(new String[]{
                        currentDate.toString(), // YYYY-MM-DD
                        weekdayName,
                        cycleLetter
                });
            }
        }

        // Reload in-memory data so letterDate() sees the new file
        data = readCSV(outputPath.toString());
    }

    /**
     * generates a calendar csv based on provided holiday
     * @param startDate
     * @param numDays
     * @param holidays
     * @throws IOException
     */
    public static void generateCalendarCsv(
            LocalDate startDate,
            int numDays,
            Set<LocalDate> holidays
    ) throws IOException {
        generateCalendarCsv(startDate, numDays, holidays, DEFAULT_CALENDAR_CSV);
    }

    /**
     * Load holiday dates from a CSV file chosen by the user.
     * <p>
     * Expected format:
     * - First column = date in YYYY-MM-DD (ISO) format.
     * - First row is treated as header and skipped.
     * @param holidayFile file containing holidays
     */
    public static Set<LocalDate> loadHolidaysFromCsv(File holidayFile) {
        Set<LocalDate> holidays = new HashSet<>();

        if (holidayFile == null) {
            return holidays;
        }

        try (FileReader fileReader = new FileReader(holidayFile);
             CSVReader csvReader = new CSVReaderBuilder(fileReader)
                     .withSkipLines(1) // skip header like "HolidayDate"
                     .build()) {

            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows) {
                if (row.length == 0) continue;
                String dateStr = row[0].trim();
                if (dateStr.isEmpty()) continue;

                try {
                    LocalDate date = LocalDate.parse(dateStr); // expects YYYY-MM-DD
                    holidays.add(date);
                } catch (Exception e) {
                    // Ignore lines that don't parse as a date
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return holidays;
    }

    /**
     * internal CSV reader used for the main calendar file.
     * skips the header row.
     * @param file path of th ecsv file
     * @return allData list of String
     */
    private static List<String[]> readCSV(String file) {
        List<String[]> allData = null;
        try {
            FileReader filereader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1) // skip header row: date,weekday,cycle_letter
                    .build();
            allData = csvReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }
}
