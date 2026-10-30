package ru.yandex.practicum;

public class WordleSystemException extends RuntimeException {
    public WordleSystemException(String message) {
        super(message);
    }

    public WordleSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}