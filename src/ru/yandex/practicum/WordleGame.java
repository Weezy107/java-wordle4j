package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    public static final int MAX_STEPS = 6;
    public static final String WIN_HINT = "+++++";

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter log;

    private boolean isWon = false;

    private final List<String> historyWords = new ArrayList<>();
    private final List<String> historyHints = new ArrayList<>();
    private final Set<String> suggestedWords = new HashSet<>();

    private final Set<Character> absentLetters = new HashSet<>();
    private final Set<Character> presentLetters = new HashSet<>();
    private final Character[] exactPositions = new Character[WordleDictionary.WORD_LENGTH];
    private final List<Set<Character>> wrongPositions = new ArrayList<>(WordleDictionary.WORD_LENGTH);

    private final Random random = new Random();

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this(dictionary, dictionary != null ? dictionary.getRandomWord() : null, log);
    }

    public WordleGame(WordleDictionary dictionary, String answer, PrintWriter log) {
        if (log == null) {
            throw new WordleSystemException("Логгер (PrintWriter) не может быть null.");
        }
        if (dictionary == null || dictionary.isEmpty()) {
            log.println("Ошибка: в игру передан пустой или null словарь.");
            throw new WordleSystemException("Словарь игры не может быть пустым.");
        }
        String normalizedAnswer = WordleDictionary.normalizeWord(answer);
        if (!WordleDictionary.isValidWordFormat(normalizedAnswer)
                || !dictionary.contains(normalizedAnswer)) {
            log.println("Ошибка: загаданное слово некорректно или отсутствует в словаре: " + answer);
            throw new WordleSystemException("Некорректное загаданное слово: " + answer);
        }

        this.dictionary = dictionary;
        this.answer = normalizedAnswer;
        this.steps = MAX_STEPS;
        this.log = log;

        for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
            wrongPositions.add(new HashSet<>());
        }

        log.println("Новая игра запущена. Загаданное слово: " + this.answer + ", попыток: " + this.steps);
    }


    public String makeStep(String userInput) throws WordNotFoundInDictionaryException, InvalidWordException {
        if (isGameOver()) {
            log.println("Ошибка: попытка сделать ход после завершения игры.");
            throw new WordleSystemException("Игра уже завершена.");
        }

        if (userInput == null || userInput.isBlank()) {
            throw new InvalidWordException("Введена пустая строка. Введите слово из 5 букв.");
        }

        String guess = WordleDictionary.normalizeWord(userInput);

        if (guess.length() != WordleDictionary.WORD_LENGTH) {
            throw new InvalidWordException("Слово должно состоять ровно из 5 букв.");
        }

        if (!WordleDictionary.isValidWordFormat(guess)) {
            throw new InvalidWordException("Слово должно содержать только русские буквы.");
        }

        // Проверяем совпадение с правильным ответом сразу, как рекомендовано в ТЗ
        if (!guess.equals(answer) && !dictionary.contains(guess)) {
            throw new WordNotFoundInDictionaryException("Слово \"" + guess + "\" не найдено в словаре.");
        }

        steps--;
        if (steps < 0) {
            log.println("Критическая ошибка: количество шагов стало отрицательным.");
            throw new WordleSystemException("Некорректное состояние игры: steps < 0");
        }

        String hint = guess.equals(answer) ? WIN_HINT : WordleDictionary.compareWords(guess, answer);

        historyWords.add(guess);
        historyHints.add(hint);
        suggestedWords.add(guess);
        updateConstraints(guess, hint);

        if (WIN_HINT.equals(hint)) {
            isWon = true;
            log.println("Игрок угадал слово '" + answer + "'! Осталось попыток: " + steps);
        } else {
            log.println("Ход: " + guess + " -> " + hint + " (осталось попыток: " + steps + ")");
        }

        return hint;
    }

    public String getHint() {
        if (isGameOver()) {
            log.println("Ошибка: запрос подсказки после окончания игры.");
            throw new WordleSystemException("Невозможно получить подсказку: игра уже завершена.");
        }

        List<String> matchingWords = new ArrayList<>();
        for (String candidate : dictionary.getWords()) {
            if (!historyWords.contains(candidate) && isWordMatchingClues(candidate)) {
                matchingWords.add(candidate);
            }
        }

        if (matchingWords.isEmpty()) {
            log.println("Критическая ошибка: список подходящих слов для подсказки пуст!");
            throw new WordleSystemException("Не найдено ни одного подходящего слова для подсказки.");
        }

        // Избегаем повторной выдачи уже предложенных вариантов, если есть другие кандидаты
        List<String> freshCandidates = new ArrayList<>();
        for (String word : matchingWords) {
            if (!suggestedWords.contains(word)) {
                freshCandidates.add(word);
            }
        }

        List<String> pool = freshCandidates.isEmpty() ? matchingWords : freshCandidates;
        String chosenHint = pool.get(random.nextInt(pool.size()));
        suggestedWords.add(chosenHint);

        log.println("Сгенерирована подсказка: " + chosenHint
                + " (подходящих вариантов: " + matchingWords.size() + ")");
        return chosenHint;
    }

    private void updateConstraints(String guess, String hint) {
        for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
            char c = guess.charAt(i);
            char h = hint.charAt(i);

            if (h == '+') {
                exactPositions[i] = c;
                presentLetters.add(c);
                absentLetters.remove(c);
            } else if (h == '^') {
                wrongPositions.get(i).add(c);
                presentLetters.add(c);
                absentLetters.remove(c);
            }
        }

        // Затем обрабатываем неподходящие буквы ('-')
        for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
            char c = guess.charAt(i);
            char h = hint.charAt(i);

            if (h == '-') {
                if (presentLetters.contains(c)) {
                    // Буква есть в слове, но повторная буква на этой позиции лишняя
                    wrongPositions.get(i).add(c);
                } else {
                    absentLetters.add(c);
                }
            }
        }
    }

    private boolean isWordMatchingClues(String word) {
        for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
            char c = word.charAt(i);

            if (absentLetters.contains(c)) {
                return false;
            }
            if (exactPositions[i] != null && exactPositions[i] != c) {
                return false;
            }
            if (wrongPositions.get(i).contains(c)) {
                return false;
            }
        }

        for (char requiredChar : presentLetters) {
            if (word.indexOf(requiredChar) == -1) {
                return false;
            }
        }

        return true;
    }

    public boolean isWon() {
        return isWon;
    }

    public boolean isGameOver() {
        return isWon || steps <= 0;
    }

    public int getSteps() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getHistoryWords() {
        return Collections.unmodifiableList(historyWords);
    }

    public List<String> getHistoryHints() {
        return Collections.unmodifiableList(historyHints);
    }
}