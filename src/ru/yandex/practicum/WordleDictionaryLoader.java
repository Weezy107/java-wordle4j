package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        if (log == null) {
            throw new WordleSystemException("Логгер (PrintWriter) не может быть null.");
        }
        this.log = log;
    }

    public WordleDictionary loadDictionary(String filename) {
        if (filename == null || filename.isBlank()) {
            log.println("Ошибка: передано пустое имя файла словаря.");
            throw new WordleSystemException("Имя файла словаря не может быть пустым.");
        }

        Set<String> uniqueWords = new LinkedHashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = WordleDictionary.normalizeWord(line);
                if (WordleDictionary.isValidWordFormat(normalized)) {
                    uniqueWords.add(normalized);
                }
            }
        } catch (IOException e) {
            log.println("Ошибка при чтении файла словаря: " + filename);
            e.printStackTrace(log);
            throw new WordleSystemException("Не удалось загрузить словарь из файла: " + filename, e);
        }

        if (uniqueWords.isEmpty()) {
            log.println("Ошибка: в файле " + filename + " не найдено подходящих слов из 5 букв.");
            throw new WordleSystemException("Словарь пуст после фильтрации файла: " + filename);
        }

        List<String> wordList = new ArrayList<>(uniqueWords);
        log.println("Словарь успешно загружен из файла '" + filename + "'. Количество слов: " + wordList.size());
        return new WordleDictionary(wordList, log);
    }
}