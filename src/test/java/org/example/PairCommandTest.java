package org.example;

import org.example.Bot.Commands.Factories.CommandFactory;
import org.example.Bot.Commands.PairCommand;
import org.example.DAO.Dish;
import org.example.DAO.DishDAO;
import org.example.DAO.WineDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PairCommandTest {

    @Mock
    private WineDAO wineDAO;

    @Mock
    private DishDAO dishDAO;

    @Mock
    private Map<Long, CommandFactory.PairingContext> pairingContexts;

    private PairCommand pairCommand;

    private final long testChatId = 12345L;
    private final String testStringParam = "test-string";
    private final long testLongParam = 0L;

    @BeforeEach
    void setUp() {
        pairCommand = new PairCommand(
                wineDAO,
                dishDAO,
                testStringParam,
                testLongParam,
                pairingContexts
        );
    }

   @Test
    void execute_ShouldReturnEmptyResponse_WhenWineNotExists() throws SQLException {

        String wineName = "Несуществующее вино";
        when(wineDAO.findPairings(wineName)).thenReturn(List.of());

        SendMessage result = pairCommand.execute(String.valueOf(testChatId), wineName);

        assertNotNull(result, "Ответ не должен быть null");
        assertFalse(result.getText().isEmpty(), "Ответ не должен быть пустым");
        verify(pairingContexts, never()).put(anyLong(), any());
    }

    @Test
    void execute_ShouldHandleMultiplePairingsCorrectly() throws SQLException {

        String wineName = "test-string"; // Используем то же имя, что и в PairCommand
        List<String> pairingNames = List.of("Рыба", "Мороженое", "Чизкейк");

        Dish fishDish = new Dish("Рыба", Dish.DishCategory.Рыба, 3, 4);
        Dish iceCreamDish = new Dish("Мороженое", Dish.DishCategory.Десерт, 2, 3);
        Dish cheesecakeDish = new Dish("Чизкейк", Dish.DishCategory.Десерт, 5, 5);

        when(wineDAO.findPairings("test-string")).thenReturn(pairingNames);
        when(dishDAO.getAllDishes()).thenReturn(List.of(fishDish, iceCreamDish, cheesecakeDish));

        SendMessage result = pairCommand.execute(String.valueOf(testChatId), wineName);

        System.out.println("=== REAL RESPONSE ===");
        System.out.println(result.getText());
        System.out.println("====================");

        assertNotNull(result, "Ответ не должен быть null");

        String responseText = result.getText().toLowerCase();
        assertTrue(responseText.contains(wineName.toLowerCase()),
                "Ответ должен содержать название вина");

        assertTrue(responseText.contains("рыба"), "Должна быть Рыба");
        assertTrue(responseText.contains("мороженое"), "Должно быть Мороженое");
        assertTrue(responseText.contains("чизкейк"), "Должен быть Чизкейк");
    }
}