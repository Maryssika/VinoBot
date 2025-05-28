package org.example;

import java.util.Objects;

public class WineAddResult {
    private final boolean success;
    private final String message;

    public WineAddResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Геттеры
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    // equals и hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WineAddResult that = (WineAddResult) o;
        return success == that.success &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message);
    }

    // toString
    @Override
    public String toString() {
        return "WineAddResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}