package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class RatingService {
    private final FavoritesRepository favoritesRepo;

    public RatingService(FavoritesRepository favoritesRepo) {
        this.favoritesRepo = favoritesRepo;
    }

    public SendMessage processRating(Long chatId, String wine, String dish,
                                     String rating, String confirmation) {
        if ("плохо".equals(rating)) {
            return new SendMessage(chatId.toString(),
                    "Спасибо за оценку! Сочетание помечено как неподходящее.");
        }

        if (favoritesRepo.exists(chatId, wine, dish)) {
            return new SendMessage(chatId.toString(),
                    "Это сочетание уже есть в вашем избранном!");
        }

        if ("Да".equals(confirmation)) {
            favoritesRepo.save(chatId, wine, dish);
            return new SendMessage(chatId.toString(),
                    "Сочетание успешно добавлено в избранное!");
        }

        return new SendMessage(chatId.toString(), "Оценка принята");
    }
}