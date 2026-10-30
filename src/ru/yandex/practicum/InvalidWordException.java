package ru.yandex.practicum;

public class InvalidWordException extends WordleGameException {
    public InvalidWordException(String message) {
        super(message);
    }
}