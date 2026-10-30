package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleDictionary {

    public static final int WORD_LENGTH = 5;

    private final List<String> words;
    private final PrintWriter log;
    private final Random random = new Random();

    public WordleDictionary(List<String> words, PrintWriter log) {
        if (log == null) {
            throw new WordleSystemException("Логгер (PrintWriter) не может быть null.");
        }
        if (words == null || words.isEmpty()) {
            log.println("Критическая ошибка: попытка создать пустой словарь.");
            throw new WordleSystemException("Словарь не может быть пустым.");
        }
        this.words = new ArrayList<>(words);
        this.log = log;
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    public int size() {
        return words.size();
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public boolean contains(String word) {
        if (word == null) {
            return false;
        }
        return words.contains(normalizeWord(word));
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            log.println("Ошибка: запрос случайного слова из пустого словаря.");
            throw new WordleSystemException("Невозможно выбрать слово: словарь пуст.");
        }
        return words.get(random.nextInt(words.size()));
    }

    public static String normalizeWord(String word) {
        if (word == null) {
            return "";
        }
        return word.trim().toLowerCase().replace('ё', 'е');
    }

    public static boolean isValidWordFormat(String word) {
        if (word == null || word.length() != WORD_LENGTH) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if ((c < 'а' || c > 'я') && c != 'ё') {
                return false;
            }
        }
        return true;
    }

    public static String compareWords(String guess, String answer) {
        if (guess == null || answer == null
                || guess.length() != WORD_LENGTH || answer.length() != WORD_LENGTH) {
            throw new WordleSystemException(
                    "Некорректная длина слов при сравнении: guess=" + guess + ", answer=" + answer
            );
        }

        char[] resultChars = new char[WORD_LENGTH];
        boolean[] answerLetterUsed = new boolean[WORD_LENGTH];

        // 1-й проход: ищем точные совпадения ('+')
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                resultChars[i] = '+';
                answerLetterUsed[i] = true;
            }
        }

        // 2-й проход: ищем буквы не на своих местах ('^') и отсутствующие буквы ('-')
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (resultChars[i] == '+') {
                continue;
            }
            char c = guess.charAt(i);
            boolean foundInOtherPosition = false;
            for (int j = 0; j < WORD_LENGTH; j++) {
                if (!answerLetterUsed[j] && answer.charAt(j) == c) {
                    foundInOtherPosition = true;
                    answerLetterUsed[j] = true;
                    break;
                }
            }
            resultChars[i] = foundInOtherPosition ? '^' : '-';
        }

        StringBuilder result = new StringBuilder(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; i++) {
            result.append(resultChars[i]);
        }
        return result.toString();
    }
}