package org.example.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelFavoritesManager {
    private static final String FILE_NAME = "favorites.xlsx";
    private static final String SHEET_NAME = "Pairings";

    public static PairingAddResult addFavorite(String wineName, String dishDescription) throws IOException {
        // Проверяем существование файла
        File file = new File(FILE_NAME);
        boolean fileExists = file.exists();

        // Если файл существует, проверяем наличие сочетания
        if (fileExists) {
            try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet != null) {
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row != null) {
                            String existingWine = row.getCell(0).getStringCellValue();
                            String existingDish = row.getCell(1).getStringCellValue();
                            if (existingWine.equalsIgnoreCase(wineName) &&
                                    existingDish.equalsIgnoreCase(dishDescription)) {
                                return new PairingAddResult(false, "Сочетание уже есть в избранном:\n" +
                                        "🍷 " + existingWine + "\n" +
                                        "🍽 " + existingDish);
                            }
                        }
                    }
                }
            }
        }

        // Если сочетание не найдено или файла нет - добавляем
        Workbook workbook;
        if (fileExists) {
            workbook = new XSSFWorkbook(new FileInputStream(file));
        } else {
            workbook = new XSSFWorkbook();
        }

        Sheet sheet = workbook.getSheet(SHEET_NAME);
        if (sheet == null) {
            sheet = workbook.createSheet(SHEET_NAME);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Wine");
            headerRow.createCell(1).setCellValue("Dish");
        }

        int lastRow = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRow + 1);
        row.createCell(0).setCellValue(wineName);
        row.createCell(1).setCellValue(dishDescription);

        try (FileOutputStream outputStream = new FileOutputStream(FILE_NAME)) {
            workbook.write(outputStream);
        }
        workbook.close();

        return new PairingAddResult(true, "Сочетание успешно добавлено в избранное!");
    }

    public static List<String> getFavorites() throws IOException {
        List<String> favorites = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return favorites;
        }

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                return favorites;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    favorites.add("🍷 " + row.getCell(0).getStringCellValue() +
                            "\n🍽 " + row.getCell(1).getStringCellValue());
                }
            }
        }
        return favorites;
    }

    public static class PairingAddResult {
        private final boolean success;
        private final String message;

        public PairingAddResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}