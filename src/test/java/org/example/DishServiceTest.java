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
class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private WinePairingService winePairingService;

    @InjectMocks
    private DishService dishService;

    @Test
    void addDish_ShouldSuccess_WhenValidDataAndNoDuplicate() {
        // Arrange
        Dish newDish = createTestDish("Утиная грудка", "Мясо", 3, 4, 45,
                "Утка, апельсины, специи", "Запечь утку с соусом");

        when(dishRepository.existsByName("Утиная грудка")).thenReturn(false);
        when(dishRepository.save(any(Dish.class))).thenReturn(newDish);

        // Act
        DishAddResult result = dishService.addDish(newDish);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Блюдо успешно добавлено", result.getMessage());
        verify(dishRepository).save(newDish);
        verify(winePairingService).createPairingsForDish(newDish);
    }

    @Test
    void addDish_ShouldFail_WhenDuplicateExists() {
        // Arrange
        Dish duplicateDish = createTestDish("Утиная грудка", "Мясо", 3, 4, 45, null, null);
        when(dishRepository.existsByName("Утиная грудка")).thenReturn(true);

        // Act
        DishAddResult result = dishService.addDish(duplicateDish);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Блюдо 'Утиная грудка' уже есть в базе", result.getMessage());
        verify(dishRepository, never()).save(any());
        verify(winePairingService, never()).createPairingsForDish(any());
    }

    @Test
    void addDish_ShouldFail_WhenInvalidFatLevel() {
        // Arrange
        Dish invalidDish = createTestDish("Утиная грудка", "Мясо", 6, 4, 45, null, null);

        // Act
        DishAddResult result = dishService.addDish(invalidDish);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Некорректное значение: жирность должна быть от 1 до 5", result.getMessage());
        verify(dishRepository, never()).existsByName(any());
        verify(dishRepository, never()).save(any());
    }

    @Test
    void addDish_ShouldFail_WhenEmptyName() {
        // Arrange
        Dish invalidDish = createTestDish("", "Мясо", 3, 4, 45, null, null);

        // Act
        DishAddResult result = dishService.addDish(invalidDish);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Название блюда не может быть пустым", result.getMessage());
    }

    @Test
    void addDish_ShouldAutoDetectCategory_WhenCategoryEmpty() {
        // Arrange
        Dish dishWithEmptyCategory = createTestDish("Стейк Рибай", "", 4, 5, 30, null, null);
        when(dishRepository.existsByName("Стейк Рибай")).thenReturn(false);
        when(dishRepository.save(any(Dish.class))).thenReturn(dishWithEmptyCategory);

        // Act
        DishAddResult result = dishService.addDish(dishWithEmptyCategory);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Мясо", dishWithEmptyCategory.getCategory());
        verify(dishRepository).save(dishWithEmptyCategory);
    }

    private Dish createTestDish(String name, String category, int fatLevel, int proteinLevel,
                                int cookingTime, String ingredients, String recipe) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setCategory(category);
        dish.setFatLevel(fatLevel);
        dish.setProteinLevel(proteinLevel);
        dish.setCookingTime(cookingTime);
        dish.setIngredients(ingredients);
        dish.setRecipe(recipe);
        return dish;
    }
}