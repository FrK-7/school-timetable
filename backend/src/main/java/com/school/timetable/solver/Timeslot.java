package com.school.timetable.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Timeslot {
    private int day;  // 0=Monday, 1=Tuesday, ...
    private int hour; // 0=first hour, 1=second hour, ...

    public String getDayName() {
        return switch (day) {
            case 0 -> "Lunedì";
            case 1 -> "Martedì";
            case 2 -> "Mercoledì";
            case 3 -> "Giovedì";
            case 4 -> "Venerdì";
            case 5 -> "Sabato";
            default -> "Giorno " + day;
        };
    }
}
