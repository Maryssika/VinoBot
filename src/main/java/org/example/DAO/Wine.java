package org.example.DAO;

import java.util.Objects;

/**
 * Класс, представляющий вино с его характеристиками.
 * Содержит информацию о названии, типе, вкусовых качествах,
 * регионе производства и других параметрах вина.
 */
public class Wine {

    private int id;
    private String name;
    private WineType type;
    private int tannins;
    private int acidity;
    private String region;
    private int vintage;
    private String description;

    public Wine() {

    }

    /**
     * Перечисление возможных типов вин
     */
    public enum WineType {
        Красное, Белое, Розовое, Десертное
    }

    /**
     * Основной конструктор с минимально необходимыми параметрами
     * @param name название вина
     * @param type тип вина
     * @param tannins уровень танинов (1-5)
     * @param acidity уровень кислотности (1-5)
     * @throws IllegalArgumentException если параметры не соответствуют ограничениям
     */
    public Wine(String name, WineType type, int tannins, int acidity) {
        this.name = name;
        this.type = type;
        setTannins(tannins);
        setAcidity(acidity);
    }

    // Геттеры и сеттеры с валидацией

    /**
     * @return уникальный идентификатор вина
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор вина
     * @param id новый идентификатор
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return название вина
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название вина
     * @param name новое название
     * @throws IllegalArgumentException если название пустое
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название вина не может быть пустым");
        }
        this.name = name;
    }

    /**
     * @return тип вина
     */
    public WineType getType() {
        return type;
    }

    /**
     * Устанавливает тип вина
     * @param type новый тип
     * @throws NullPointerException если тип null
     */
    public void setType(WineType type) {
        this.type = Objects.requireNonNull(type, "Тип вина не может быть null");
    }

    /**
     * @return уровень танинов (1-5)
     */
    public int getTannins() {
        return tannins;
    }

    /**
     * Устанавливает уровень танинов
     * @param tannins значение от 1 до 5
     * @throws IllegalArgumentException если значение вне диапазона
     */
    public void setTannins(int tannins) {
        if (tannins < 1 || tannins > 5) {
            throw new IllegalArgumentException("Уровень танинов должен быть между 1 и 5");
        }
        this.tannins = tannins;
    }

    /**
     * @return уровень кислотности (1-5)
     */
    public int getAcidity() {
        return acidity;
    }

    /**
     * Устанавливает уровень кислотности
     * @param acidity значение от 1 до 5
     * @throws IllegalArgumentException если значение вне диапазона
     */
    public void setAcidity(int acidity) {
        if (acidity < 1 || acidity > 5) {
            throw new IllegalArgumentException("Уровень кислотности должен быть между 1 и 5");
        }
        this.acidity = acidity;
    }

    /**
     * @return регион производства вина
     */
    public String getRegion() {
        return region;
    }

    /**
     * Устанавливает регион производства
     * @param region название региона
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return год урожая винограда
     */
    public int getVintage() {
        return vintage;
    }

    /**
     * Устанавливает год урожая
     * @param vintage год (1900-текущий год)
     * @throws IllegalArgumentException если год вне допустимого диапазона
     */
    public void setVintage(int vintage) {
        if (vintage < 1900 || vintage > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Некорректный год урожая");
        }
        this.vintage = vintage;
    }

    /**
     * @return описание вкусовых характеристик
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание вина
     * @param description текст описания
     */
    public void setDescription(String description) {
        this.description = description;
    }

    // Методы для определения характеристик

    /**
     * Проверяет, является ли вино полнотелым
     * @return true если уровень танинов 4 или 5
     */
    public boolean isFullBodied() {
        return tannins >= 4;
    }

    /**
     * Проверяет, является ли вино высококислотным
     * @return true если уровень кислотности 4 или 5
     */
    public boolean isHighAcidity() {
        return acidity >= 4;
    }

    /**
     * Сравнивает вина по идентификатору, названию и типу
     * @param o объект для сравнения
     * @return true если вина одинаковые
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wine wine = (Wine) o;
        return id == wine.id &&
                Objects.equals(name, wine.name) &&
                type == wine.type;
    }

    /**
     * @return хэш-код на основе id, названия и типа
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }

    /**
     * @return строковое представление вина с основной информацией
     */
    @Override
    public String toString() {
        return String.format(
                "*%s* (%s, %d)\n\n" +
                        "🔹 *Регион:* %s\n" +
                        "🔹 *Танины:* %d/5\n" +
                        "🔹 *Кислотность:* %d/5\n\n" +
                        "%s\n"+
                        "------------",
                name, type, vintage,
                region != null ? region : "не указан",
                tannins, acidity,
                description != null ? description : "Описание отсутствует"
        );
    }
}