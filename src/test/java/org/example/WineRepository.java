package org.example;

public interface WineRepository {
    boolean existsByNameAndYear(String name, Integer year);
    Wine save(Wine wine);
}