package org.example;

public class Dish {
    private String name;
    private String category;
    private int fatLevel;
    private int proteinLevel;
    private int cookingTime;
    private String ingredients;
    private String recipe;

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getFatLevel() { return fatLevel; }
    public void setFatLevel(int fatLevel) { this.fatLevel = fatLevel; }
    public int getProteinLevel() { return proteinLevel; }
    public void setProteinLevel(int proteinLevel) { this.proteinLevel = proteinLevel; }
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getRecipe() { return recipe; }
    public void setRecipe(String recipe) { this.recipe = recipe; }
}