package org.example;

public class DishService {
    private final DishRepository dishRepository;
    private final WinePairingService winePairingService;

    public DishService(DishRepository dishRepository, WinePairingService winePairingService) {
        this.dishRepository = dishRepository;
        this.winePairingService = winePairingService;
    }

    public DishAddResult addDish(Dish dish) {
        // Валидация
        if (dish.getName() == null || dish.getName().trim().isEmpty()) {
            return new DishAddResult(false, "Название блюда не может быть пустым");
        }
        if (dish.getFatLevel() < 1 || dish.getFatLevel() > 5) {
            return new DishAddResult(false, "Некорректное значение: жирность должна быть от 1 до 5");
        }
        if (dish.getProteinLevel() < 1 || dish.getProteinLevel() > 5) {
            return new DishAddResult(false, "Некорректное значение: белок должен быть от 1 до 5");
        }
        if (dish.getCookingTime() < 0) {
            return new DishAddResult(false, "Время приготовления не может быть отрицательным");
        }

        // Автодетект категории если не указана
        if (dish.getCategory() == null || dish.getCategory().isEmpty()) {
            dish.setCategory(detectCategory(dish.getName()));
        }

        // Проверка дубликатов
        if (dishRepository.existsByName(dish.getName())) {
            return new DishAddResult(false,
                    String.format("Блюдо '%s' уже есть в базе", dish.getName()));
        }

        // Сохранение
        Dish savedDish = dishRepository.save(dish);
        winePairingService.createPairingsForDish(savedDish);
        return new DishAddResult(true, "Блюдо успешно добавлено");
    }

    private String detectCategory(String dishName) {
        String lowerName = dishName.toLowerCase();
        if (lowerName.contains("мясо") || lowerName.contains("стейк") || lowerName.contains("утка")) {
            return "Мясо";
        } else if (lowerName.contains("лосось") || lowerName.contains("креветки") || lowerName.contains("рыба")) {
            return "Рыба";
        } else if (lowerName.contains("салат") || lowerName.contains("овощи")) {
            return "Овощи";
        }
        return "Другое";
    }
}