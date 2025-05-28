package org.example;

import java.util.Objects;

public class Wine {
    private String name;
    private String type;
    private int tannins;
    private int acidity;
    private String region;
    private Integer year;
    private String description;

    public Wine() {
    }

    public Wine(String name, String type, int tannins, int acidity,
                String region, Integer year, String description) {
        this.name = name;
        this.type = type;
        this.tannins = tannins;
        this.acidity = acidity;
        this.region = region;
        this.year = year;
        this.description = description;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTannins() {
        return tannins;
    }

    public void setTannins(int tannins) {
        this.tannins = tannins;
    }

    public int getAcidity() {
        return acidity;
    }

    public void setAcidity(int acidity) {
        this.acidity = acidity;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // equals и hashCode для корректного сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wine wine = (Wine) o;
        return tannins == wine.tannins &&
                acidity == wine.acidity &&
                Objects.equals(name, wine.name) &&
                Objects.equals(type, wine.type) &&
                Objects.equals(region, wine.region) &&
                Objects.equals(year, wine.year) &&
                Objects.equals(description, wine.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, tannins, acidity, region, year, description);
    }

    // toString для удобного вывода информации
    @Override
    public String toString() {
        return "Wine{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", tannins=" + tannins +
                ", acidity=" + acidity +
                ", region='" + region + '\'' +
                ", year=" + year +
                ", description='" + description + '\'' +
                '}';
    }
}