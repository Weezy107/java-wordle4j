package ru.yandex.practicum;

public class WordNotFoundInDictionaryException extends WordleGameException {
    public WordNotFoundInDictionaryException(String message) {
        super(message);
    }
}