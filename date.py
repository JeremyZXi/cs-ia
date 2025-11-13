import csv
import datetime as dt



# Start date
START_DATE = dt.date(2025, 9, 1)

# How many days to generate
NUM_DAYS = 365

# List of holiday dates (equivalent to Setup!A2:A49)
# Fill this with your real holidays:
HOLIDAYS = {
    # ----- September 2025 -----
    dt.date(2025, 9, 29),
    dt.date(2025, 9, 30),

    # ----- October 2025 -----
    dt.date(2025, 10, 1),
    dt.date(2025, 10, 2),
    dt.date(2025, 10, 3),
    dt.date(2025, 10, 6),   # Mid-Autumn Festival

    # ----- November 2025 -----
    dt.date(2025, 11, 7),   # PG Day (no school)
    dt.date(2025, 11, 24),  # Mid-term break

    # ----- December 2025 -----
    dt.date(2025, 12, 22),
    dt.date(2025, 12, 23),
    dt.date(2025, 12, 24),
    dt.date(2025, 12, 25),
    dt.date(2025, 12, 26),
    dt.date(2025, 12, 27),
    dt.date(2025, 12, 28),
    dt.date(2025, 12, 29),
    dt.date(2025, 12, 30),
    dt.date(2025, 12, 31),

    # ----- January 2026 -----
    dt.date(2026, 1, 1),
    dt.date(2026, 1, 2),

    # ----- February 2026 (Chinese New Year Holiday) -----
    dt.date(2026, 2, 9),
    dt.date(2026, 2, 10),
    dt.date(2026, 2, 11),
    dt.date(2026, 2, 12),
    dt.date(2026, 2, 13),
    dt.date(2026, 2, 14),
    dt.date(2026, 2, 15),
    dt.date(2026, 2, 16),
    dt.date(2026, 2, 17),
    dt.date(2026, 2, 18),
    dt.date(2026, 2, 19),
    dt.date(2026, 2, 20),
    dt.date(2026, 2, 21),
    dt.date(2026, 2, 22),
    dt.date(2026, 2, 23),

    # ----- March 2026 -----
    dt.date(2026, 3, 20),  # PG Day (no school)

    # ----- April 2026 (Spring Break & Qing Ming) -----
    dt.date(2026, 4, 5),
    dt.date(2026, 4, 6),
    dt.date(2026, 4, 7),
    dt.date(2026, 4, 8),
    dt.date(2026, 4, 9),
    dt.date(2026, 4, 10),

    # ----- May 2026 (Labor Day Holiday) -----
    dt.date(2026, 5, 1),
    dt.date(2026, 5, 2),
    dt.date(2026, 5, 3),
    dt.date(2026, 5, 4),

    # ----- June 2026 -----
    dt.date(2026, 6, 19),  # Duan Wu Festival Holiday

    # ----- July 2026 (Summer Break) -----
    # July 1 is NOT a holiday. Summer break starts July 2.
    dt.date(2026, 7, 2), dt.date(2026, 7, 3), dt.date(2026, 7, 4), dt.date(2026, 7, 5),
    dt.date(2026, 7, 6), dt.date(2026, 7, 7), dt.date(2026, 7, 8), dt.date(2026, 7, 9),
    dt.date(2026, 7, 10), dt.date(2026, 7, 11), dt.date(2026, 7, 12), dt.date(2026, 7, 13),
    dt.date(2026, 7, 14), dt.date(2026, 7, 15), dt.date(2026, 7, 16), dt.date(2026, 7, 17),
    dt.date(2026, 7, 18), dt.date(2026, 7, 19), dt.date(2026, 7, 20), dt.date(2026, 7, 21),
    dt.date(2026, 7, 22), dt.date(2026, 7, 23), dt.date(2026, 7, 24), dt.date(2026, 7, 25),
    dt.date(2026, 7, 26), dt.date(2026, 7, 27), dt.date(2026, 7, 28), dt.date(2026, 7, 29),
    dt.date(2026, 7, 30), dt.date(2026, 7, 31),
}


# 8-day cycle letters
CYCLE_LETTERS = ["A", "B", "C", "D", "E", "F", "G", "H"]

# Output CSV file name
OUTPUT_CSV = "calendar.csv"


def generate_calendar_csv(
    start_date: dt.date,
    num_days: int,
    holidays: set[dt.date],
    output_path: str,
):
    """
    Generate a CSV with columns:
        date, weekday, cycle_letter

    Rules:
      - cycle_letter advances through A..H on weekdays (Mon–Fri)
      - weekends (Sat, Sun) get '0' and do NOT advance the cycle
      - holidays get '0' and do NOT advance the cycle
    """

    current_cycle_index = 0  # tracks where we are in A..H

    with open(output_path, mode="w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        # header row
        writer.writerow(["date", "weekday", "cycle_letter"])

        for offset in range(num_days):
            current_date = start_date + dt.timedelta(days=offset)

            # Python: Monday = 0, Sunday = 6
            weekday_index = current_date.weekday()
            weekday_name = current_date.strftime("%A")

            is_weekend = weekday_index >= 5  # 5=Saturday, 6=Sunday
            is_holiday = current_date in holidays

            if is_weekend or is_holiday:
                # No cycle letter on weekends or holidays
                cycle_letter = "0"
            else:
                # Weekday, not a holiday: use and advance cycle
                cycle_letter = CYCLE_LETTERS[current_cycle_index]
                current_cycle_index = (current_cycle_index + 1) % len(CYCLE_LETTERS)

            writer.writerow([
                current_date.isoformat(),  # YYYY-MM-DD
                weekday_name,              # e.g. 'Monday'
                cycle_letter,              # A..H or '0'
            ])


if __name__ == "__main__":
    generate_calendar_csv(
        start_date=START_DATE,
        num_days=NUM_DAYS,
        holidays=HOLIDAYS,
        output_path=OUTPUT_CSV,
    )
    print(f"CSV written to {OUTPUT_CSV}")
