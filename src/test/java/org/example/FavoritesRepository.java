package org.example;

public interface FavoritesRepository {
    boolean exists(Long chatId, String wine, String dish);
    void save(Long chatId, String wine, String dish);
}