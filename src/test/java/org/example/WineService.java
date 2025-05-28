package org.example;

public class WineService {
    private final WineRepository wineRepository;

    public WineService(WineRepository wineRepository) {
        this.wineRepository = wineRepository;
    }

    public WineAddResult addWine(Wine wine) {
        // Валидация
        if (wine.getName() == null || wine.getName().trim().isEmpty()) {
            return new WineAddResult(false, "Название вина не может быть пустым");
        }
        if (wine.getTannins() < 1 || wine.getTannins() > 5) {
            return new WineAddResult(false, "Некорректное значение: танины должны быть от 1 до 5");
        }
        if (wine.getAcidity() < 1 || wine.getAcidity() > 5) {
            return new WineAddResult(false, "Некорректное значение: кислотность должна быть от 1 до 5");
        }
        if (wine.getYear() != null && wine.getYear() > java.time.Year.now().getValue()) {
            return new WineAddResult(false, "Год урожая не может быть в будущем");
        }

        // Проверка дубликатов
        if (wineRepository.existsByNameAndYear(wine.getName(), wine.getYear())) {
            return new WineAddResult(false,
                    String.format("Вино '%s %d' уже есть в базе", wine.getName(), wine.getYear()));
        }

        // Сохранение
        Wine savedWine = wineRepository.save(wine);
        return new WineAddResult(true, "Вино успешно добавлено");
    }
}