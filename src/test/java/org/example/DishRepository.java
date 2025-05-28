package org.example;

public interface DishRepository {
    boolean existsByName(String name);
    Dish save(Dish dish);
}