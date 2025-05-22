package org.example.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.DAO.Dish;
import org.example.DAO.Wine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер для работы с избранными винами и блюдами в Excel-файле.
 * Обеспечивает добавление и получение списка избранных позиций.
 */
public class ExcelFavoritesManager {
    // Имя файла для хранения избранного
    private static final String FILE_NAME = "favorites.xlsx";
    // Названия листов в Excel-файле
    private static final String WINES_SHEET = "Wines";
    private static final String DISHES_SHEET = "Dishes";

    /**
     * Добавляет вино в список избранного
     * @param wine объект вина для добавления
     * @throws IOException если произошла ошибка работы с файлом
     */
    public static void addWineToFavorites(Wine wine) throws IOException {
        addItemToFavorites(WINES_SHEET, wine.getName(), wine.toString());
    }

    /**
     * Добавляет блюдо в список избранного
     * @param dish объект блюда для добавления
     * @throws IOException если произошла ошибка работы с файлом
     */
    public static void addDishToFavorites(Dish dish) throws IOException {
        addItemToFavorites(DISHES_SHEET, dish.getName(), dish.toString());
    }

    /**
     * Внутренний метод для добавления элемента в избранное
     * @param sheetName название листа (Wines/Dishes)
     * @param name название элемента
     * @param details детали элемента
     * @throws IOException если произошла ошибка работы с файлом
     */
    private static void addItemToFavorites(String sheetName, String name, String details) throws IOException {
        Workbook workbook;
        File file = new File(FILE_NAME);

        // Открываем существующий файл или создаем новый
        if (file.exists()) {
            workbook = new XSSFWorkbook(new FileInputStream(file));
        } else {
            workbook = new XSSFWorkbook();
        }

        // Получаем или создаем лист
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            // Создаем заголовки столбцов
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Details");
        }

        // Добавляем новую запись
        int lastRow = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRow + 1);
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue(details);

        // Сохраняем изменения
        try (FileOutputStream outputStream = new FileOutputStream(FILE_NAME)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    /**
     * Получает список избранных вин
     * @return список строк с информацией о винах
     * @throws IOException если произошла ошибка работы с файлом
     */
    public static List<String> getFavoriteWines() throws IOException {
        return getFavorites(WINES_SHEET);
    }

    /**
     * Получает список избранных блюд
     * @return список строк с информацией о блюдах
     * @throws IOException если произошла ошибка работы с файлом
     */
    public static List<String> getFavoriteDishes() throws IOException {
        return getFavorites(DISHES_SHEET);
    }

    /**
     * Внутренний метод для получения списка избранного
     * @param sheetName название листа (Wines/Dishes)
     * @return список элементов избранного
     * @throws IOException если произошла ошибка работы с файлом
     */
    private static List<String> getFavorites(String sheetName) throws IOException {
        List<String> favorites = new ArrayList<>();
        File file = new File(FILE_NAME);

        // Если файла нет - возвращаем пустой список
        if (!file.exists()) {
            return favorites;
        }

        // Читаем данные из файла
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return favorites;
            }

            // Собираем все записи (пропускаем заголовок)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    favorites.add(row.getCell(0).getStringCellValue() + " - " +
                            row.getCell(1).getStringCellValue());
                }
            }
        }
        return favorites;
    }
}