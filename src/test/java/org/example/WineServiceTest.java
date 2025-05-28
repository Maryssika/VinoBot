package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WineServiceTest {

    @Mock
    private WineRepository wineRepository;

    @InjectMocks
    private WineService wineService;

    @Test
    void addNewWine_ShouldSuccess_WhenValidDataAndNoDuplicate() {
        // Arrange
        Wine newWine = createTestWine("Пино Нуар", "Красное", 3, 4, "Бургундия", 2020, "Фруктовое с нотами вишни");

        when(wineRepository.existsByNameAndYear("Пино Нуар", 2020)).thenReturn(false);
        when(wineRepository.save(any(Wine.class))).thenReturn(newWine);

        // Act
        WineAddResult result = wineService.addWine(newWine);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Вино успешно добавлено", result.getMessage());
        verify(wineRepository).save(newWine);
    }

    @Test
    void addNewWine_ShouldFail_WhenDuplicateExists() {
        // Arrange
        Wine duplicateWine = createTestWine("Пино Нуар", "Красное", 3, 4, null, 2020, null);

        when(wineRepository.existsByNameAndYear("Пино Нуар", 2020)).thenReturn(true);

        // Act
        WineAddResult result = wineService.addWine(duplicateWine);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Вино 'Пино Нуар 2020' уже есть в базе", result.getMessage());
        verify(wineRepository, never()).save(any());
    }

    @Test
    void addNewWine_ShouldFail_WhenInvalidTannins() {
        // Arrange
        Wine invalidWine = createTestWine("Пино Нуар", "Красное", 6, 4, null, 2020, null);

        // Act
        WineAddResult result = wineService.addWine(invalidWine);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Некорректное значение: танины должны быть от 1 до 5", result.getMessage());
        verify(wineRepository, never()).existsByNameAndYear(any(), anyInt());
        verify(wineRepository, never()).save(any());
    }

    @Test
    void addNewWine_ShouldFail_WhenEmptyName() {
        // Arrange
        Wine invalidWine = createTestWine("", "Красное", 3, 4, null, 2020, null);

        // Act
        WineAddResult result = wineService.addWine(invalidWine);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Название вина не может быть пустым", result.getMessage());
    }

    private Wine createTestWine(String name, String type, int tannins, int acidity,
                                String region, Integer year, String description) {
        Wine wine = new Wine();
        wine.setName(name);
        wine.setType(type);
        wine.setTannins(tannins);
        wine.setAcidity(acidity);
        wine.setRegion(region);
        wine.setYear(year);
        wine.setDescription(description);
        return wine;
    }
}