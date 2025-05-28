package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private FavoritesRepository favoritesRepo;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void ratePairing_ShouldAddToFavorites_WhenGoodRatingAndConfirmed() {
        // Arrange
        Long chatId = 123L;
        String wine = "Шардоне";
        String dish = "Рыба";
        String rating = "хорошо";
        String confirmation = "Да";

        when(favoritesRepo.exists(chatId, wine, dish)).thenReturn(false);

        // Act
        SendMessage result = ratingService.processRating(chatId, wine, dish, rating, confirmation);

        // Assert
        assertEquals("Сочетание успешно добавлено в избранное!", result.getText());
        verify(favoritesRepo).save(chatId, wine, dish);
    }

    @Test
    void ratePairing_ShouldNotAdd_WhenBadRating() {
        // Arrange
        Long chatId = 123L;
        String wine = "Шардоне";
        String dish = "Рыба";
        String rating = "плохо";

        // Act
        SendMessage result = ratingService.processRating(chatId, wine, dish, rating, null);

        // Assert
        assertEquals("Спасибо за оценку! Сочетание помечено как неподходящее.", result.getText());
        verify(favoritesRepo, never()).save(anyLong(), anyString(), anyString());
    }

    @Test
    void ratePairing_ShouldWarn_WhenAlreadyInFavorites() {
        // Arrange
        Long chatId = 123L;
        String wine = "Шардоне";
        String dish = "Рыба";
        String rating = "хорошо";
        String confirmation = "Да";

        when(favoritesRepo.exists(chatId, wine, dish)).thenReturn(true);

        // Act
        SendMessage result = ratingService.processRating(chatId, wine, dish, rating, confirmation);

        // Assert
        assertEquals("Это сочетание уже есть в вашем избранном!", result.getText());
        verify(favoritesRepo, never()).save(anyLong(), anyString(), anyString());
    }

    public static interface FavoritesRepository {
        boolean exists(Long chatId, String wine, String dish);
        void save(Long chatId, String wine, String dish);
    }

    public static class RatingService {
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
}
