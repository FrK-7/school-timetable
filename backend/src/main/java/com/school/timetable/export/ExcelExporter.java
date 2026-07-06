package com.school.timetable.export;

import com.school.timetable.model.SchoolConfig;
import com.school.timetable.repository.SchoolConfigRepository;
import com.school.timetable.solver.Lesson;
import com.school.timetable.solver.TimetableSolution;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExcelExporter {

    private final SchoolConfigRepository configRepo;

    private static final String[] DAY_NAMES = {
            "LUNEDI", "MARTEDI", "MERCOLEDI", "GIOVEDI", "VENERDI", "SABATO"
    };

    public byte[] export(TimetableSolution solution) throws IOException {
        SchoolConfig config = configRepo.findAll().stream().findFirst()
                .orElseThrow();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createMainSheet(workbook, solution, config);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createMainSheet(Workbook workbook, TimetableSolution solution, SchoolConfig config) {
        Sheet sheet = workbook.createSheet("Orario");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle subjectHeaderStyle = createSubjectHeaderStyle(workbook);
        CellStyle borderStyle = createBorderStyle(workbook);
        CellStyle centeredStyle = createCenteredStyle(workbook);

        int daysPerWeek = config.getDaysPerWeek();
        int hoursPerDay = config.getHoursPerDay();

        // Group lessons by teacher
        Map<String, List<Lesson>> byTeacher = solution.getLessons().stream()
                .filter(l -> l.getTimeslot() != null)
                .collect(Collectors.groupingBy(Lesson::getTeacherName));

        // Group teachers by subject
        Map<String, List<String>> teachersBySubject = solution.getLessons().stream()
                .collect(Collectors.groupingBy(Lesson::getSubjectName,
                        Collectors.mapping(Lesson::getTeacherName, Collectors.toCollection(TreeSet::new))))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue()),
                        (a, b) -> a, TreeMap::new));

        // === ROW 0: Header row with merged day columns ===
        Row headerRow1 = sheet.createRow(0);
        Cell docCell = headerRow1.createCell(0);
        docCell.setCellValue("DOCENTE");
        docCell.setCellStyle(headerStyle);

        Cell assocCell = headerRow1.createCell(1);
        assocCell.setCellValue("CLASSE (ORE)");
        assocCell.setCellStyle(headerStyle);

        int colStart = 2;
        for (int day = 0; day < daysPerWeek; day++) {
            int mergeFrom = colStart + day * hoursPerDay;
            int mergeTo = mergeFrom + hoursPerDay - 1;
            Cell dayCell = headerRow1.createCell(mergeFrom);
            dayCell.setCellValue(DAY_NAMES[day]);
            dayCell.setCellStyle(headerStyle);
            if (mergeTo > mergeFrom) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, mergeFrom, mergeTo));
            }
        }

        // === ROW 1: Sub-header with hour numbers ===
        Row headerRow2 = sheet.createRow(1);
        headerRow2.createCell(0).setCellStyle(headerStyle);
        headerRow2.createCell(1).setCellStyle(headerStyle);
        for (int day = 0; day < daysPerWeek; day++) {
            for (int hour = 0; hour < hoursPerDay; hour++) {
                int col = colStart + day * hoursPerDay + hour;
                Cell cell = headerRow2.createCell(col);
                cell.setCellValue(hour + 1);
                cell.setCellStyle(headerStyle);
            }
        }

        // === DATA ROWS: grouped by subject ===
        int rowNum = 2;

        for (Map.Entry<String, List<String>> subjectEntry : teachersBySubject.entrySet()) {
            String subjectName = subjectEntry.getKey();
            List<String> teachers = subjectEntry.getValue();

            // Subject header row
            Row subjectRow = sheet.createRow(rowNum++);
            Cell subjectCell = subjectRow.createCell(0);
            subjectCell.setCellValue(subjectName.toUpperCase());
            subjectCell.setCellStyle(subjectHeaderStyle);
            // Merge subject header across first 2 columns
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            // Teacher rows for this subject
            for (String teacherName : teachers) {
                Row row = sheet.createRow(rowNum++);

                // Col 0: Teacher name
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(teacherName);
                nameCell.setCellStyle(borderStyle);

                // Col 1: Class assignments with hours
                List<Lesson> teacherLessons = byTeacher.getOrDefault(teacherName, Collections.emptyList());
                // Only lessons for this subject
                List<Lesson> subjectLessons = teacherLessons.stream()
                        .filter(l -> l.getSubjectName().equals(subjectName))
                        .toList();

                String classHours = buildClassHoursString(subjectLessons);
                Cell classCell = row.createCell(1);
                classCell.setCellValue(classHours);
                classCell.setCellStyle(borderStyle);

                // Cols 2+: Timetable grid (class name in each slot)
                Map<String, Lesson> slotMap = subjectLessons.stream()
                        .collect(Collectors.toMap(
                                l -> l.getTimeslot().getDay() + "-" + l.getTimeslot().getHour(),
                                l -> l, (a, b) -> a));

                for (int day = 0; day < daysPerWeek; day++) {
                    for (int hour = 0; hour < hoursPerDay; hour++) {
                        int col = colStart + day * hoursPerDay + hour;
                        Cell cell = row.createCell(col);
                        Lesson lesson = slotMap.get(day + "-" + hour);
                        if (lesson != null) {
                            cell.setCellValue(lesson.getClassName());
                        }
                        cell.setCellStyle(centeredStyle);
                    }
                }
            }
        }

        // Auto-size columns
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 6000);
        for (int day = 0; day < daysPerWeek; day++) {
            for (int hour = 0; hour < hoursPerDay; hour++) {
                sheet.setColumnWidth(colStart + day * hoursPerDay + hour, 1200);
            }
        }
    }

    private String buildClassHoursString(List<Lesson> lessons) {
        Map<String, Long> hoursByClass = lessons.stream()
                .collect(Collectors.groupingBy(Lesson::getClassName, TreeMap::new, Collectors.counting()));

        return hoursByClass.entrySet().stream()
                .map(e -> e.getKey() + "(" + e.getValue() + "h)")
                .collect(Collectors.joining(" "));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSubjectHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCenteredStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        return style;
    }
}
