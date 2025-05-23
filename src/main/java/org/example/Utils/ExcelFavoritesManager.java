package org.example.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelFavoritesManager {
    private static final String FILE_NAME = "favorites.xlsx";
    private static final String SHEET_NAME = "Pairings";

    public static void addFavorite(String wineName, String dishName) throws IOException {
        Workbook workbook;
        File file = new File(FILE_NAME);

        if (file.exists()) {
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
        row.createCell(1).setCellValue(dishName);

        try (FileOutputStream outputStream = new FileOutputStream(FILE_NAME)) {
            workbook.write(outputStream);
        }
        workbook.close();
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
                    favorites.add(row.getCell(0).getStringCellValue() + " + " +
                            row.getCell(1).getStringCellValue());
                }
            }
        }
        return favorites;
    }
}