package org.example;

public class DishAddResult {
    private final boolean success;
    private final String message;

    public DishAddResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}