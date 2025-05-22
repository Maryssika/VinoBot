package org.example.DAO;

import java.util.Objects;

/**
 * Класс, представляющий блюдо с его характеристиками.
 * Содержит информацию о названии, категории, пищевой ценности,
 * времени приготовления и других параметрах блюда.
 */
public class Dish {

    private int id;
    private String name;
    private DishCategory category;
    private int fatContent;
    private int proteinContent;
    private int cookingTime;
    private String ingredients;
    private String recipe;

    /**
     * Перечисление возможных категорий блюд
     */
    public enum DishCategory {
        Мясо, Рыба, Овощи, Сыр, Десерт
    }

    /**
     * Конструктор по умолчанию
     */
    public Dish() {
    }

    /**
     * Основной конструктор с минимально необходимыми параметрами
     * @param name название блюда
     * @param category категория блюда
     * @param fatContent уровень жирности (1-5)
     * @param proteinContent уровень белка (1-5)
     * @throws IllegalArgumentException если параметры не соответствуют ограничениям
     */
    public Dish(String name, DishCategory category, int fatContent, int proteinContent) {
        this.name = name;
        this.category = category;
        setFatContent(fatContent);
        setProteinContent(proteinContent);
    }

    // Геттеры и сеттеры с валидацией

    /**
     * @return уникальный идентификатор блюда
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор блюда
     * @param id новый идентификатор
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return название блюда
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название блюда
     * @param name новое название
     * @throws IllegalArgumentException если название пустое
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название блюда не может быть пустым");
        }
        this.name = name;
    }

    /**
     * @return категория блюда
     */
    public DishCategory getCategory() {
        return category;
    }

    /**
     * Устанавливает категорию блюда
     * @param category новая категория
     * @throws NullPointerException если категория null
     */
    public void setCategory(DishCategory category) {
        this.category = Objects.requireNonNull(category, "Категория блюда не может быть null");
    }

    /**
     * @return уровень жирности (1-5)
     */
    public int getFatContent() {
        return fatContent;
    }

    /**
     * Устанавливает уровень жирности
     * @param fatContent значение от 1 до 5
     * @throws IllegalArgumentException если значение вне диапазона
     */
    public void setFatContent(int fatContent) {
        if (fatContent < 1 || fatContent > 5) {
            throw new IllegalArgumentException("Уровень жирности должен быть между 1 и 5");
        }
        this.fatContent = fatContent;
    }

    /**
     * @return уровень содержания белка (1-5)
     */
    public int getProteinContent() {
        return proteinContent;
    }

    /**
     * Устанавливает уровень содержания белка
     * @param proteinContent значение от 1 до 5
     * @throws IllegalArgumentException если значение вне диапазона
     */
    public void setProteinContent(int proteinContent) {
        if (proteinContent < 1 || proteinContent > 5) {
            throw new IllegalArgumentException("Уровень белка должен быть между 1 и 5");
        }
        this.proteinContent = proteinContent;
    }

    /**
     * @return время приготовления в минутах
     */
    public int getCookingTime() {
        return cookingTime;
    }

    /**
     * Устанавливает время приготовления
     * @param cookingTime время в минутах (не может быть отрицательным)
     * @throws IllegalArgumentException если время отрицательное
     */
    public void setCookingTime(int cookingTime) {
        if (cookingTime < 0) {
            throw new IllegalArgumentException("Время приготовления не может быть отрицательным");
        }
        this.cookingTime = cookingTime;
    }

    /**
     * @return список ингредиентов блюда
     */
    public String getIngredients() {
        return ingredients;
    }

    /**
     * Устанавливает список ингредиентов
     * @param ingredients строка с ингредиентами
     */
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * @return рецепт приготовления блюда
     */
    public String getRecipe() {
        return recipe;
    }

    /**
     * Устанавливает рецепт приготовления
     * @param recipe текст рецепта
     */
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    // Методы для определения характеристик

    /**
     * Проверяет, является ли блюдо жирным
     * @return true если уровень жирности 4 или 5
     */
    public boolean isRichInFat() {
        return fatContent >= 4;
    }

    /**
     * Проверяет, является ли блюдо богатым белком
     * @return true если уровень белка 4 или 5
     */
    public boolean isHighProtein() {
        return proteinContent >= 4;
    }

    /**
     * Сравнивает блюда по идентификатору, названию и категории
     * @param o объект для сравнения
     * @return true если блюда одинаковые
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id &&
                Objects.equals(name, dish.name) &&
                category == dish.category;
    }

    /**
     * @return хэш-код на основе id, названия и категории
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, category);
    }

    /**
     * @return строковое представление блюда с основной информацией
     */
    @Override
    public String toString() {
        return String.format(
                "*%s* (%s)\n\n" +
                        "⏱ *Время приготовления:* %s\n" +
                        "🔹 *Жирность:* %d/5\n" +
                        "🔹 *Белок:* %d/5\n\n" +
                        "🍽 *Ингредиенты:*\n%s\n\n" +
                        "📝 *Рецепт:*\n%s",
                name, category,
                getCookingTimeFormatted(),
                fatContent, proteinContent,
                ingredients != null ? ingredients : "не указаны",
                recipe != null ? recipe : "Рецепт отсутствует"
        );
    }
    // Дополнительные методы

    /**
     * Возвращает время приготовления в удобочитаемом формате
     * @return строка вида "2 ч 30 мин" или "45 мин"
     */
    public String getCookingTimeFormatted() {
        if (cookingTime == 0) return "Не указано";
        int hours = cookingTime / 60;
        int minutes = cookingTime % 60;

        if (hours > 0) {
            return String.format("%d ч %d мин", hours, minutes);
        } else {
            return String.format("%d мин", minutes);
        }
    }
}