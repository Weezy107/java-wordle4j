package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class Wordle {

    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(new FileWriter(LOG_FILE, StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {

            try {
                log.println("=== Запуск приложения Wordle ===");

                WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
                WordleDictionary dictionary = loader.loadDictionary(DICTIONARY_FILE);
                WordleGame game = new WordleGame(dictionary, log);

                runGameLoop(game, scanner, log);

                log.println("=== Завершение приложения Wordle ===");
            } catch (Exception e) {
                log.println("Критическая ошибка во время работы программы: " + e.getMessage());
                e.printStackTrace(log);
                System.out.println("Произошла внутренняя ошибка программы. Подробности записаны в лог-файл.");
            }

        } catch (IOException e) {
            System.err.println("Не удалось создать лог-файл: " + e.getMessage());
        }
    }

    private static void runGameLoop(WordleGame game, Scanner scanner, PrintWriter log) {
        System.out.println("Добро пожаловать в игру Wordle!");
        System.out.println("Угадайте слово из 5 букв за " + WordleGame.MAX_STEPS + " попыток.");
        System.out.println("Подсказки: '+' — точное совпадение, '^' — буква на другом месте, '-' — буквы нет в слове.");
        System.out.println("Нажмите Enter на пустой строке, чтобы компьютер сделал ход-подсказку за вас.\n");

        while (!game.isGameOver()) {
            System.out.print("Введите слово (осталось попыток: " + game.getSteps() + ") > ");
            if (!scanner.hasNextLine()) {
                log.println("Поток ввода закрыт пользователем.");
                break;
            }

            String userInput = scanner.nextLine().trim();

            try {
                if (userInput.isEmpty()) {
                    userInput = game.getHint();
                    System.out.println("> " + userInput);
                } else {
                    System.out.println("> " + WordleDictionary.normalizeWord(userInput));
                }

                String hint = game.makeStep(userInput);
                System.out.println("> " + hint);
                System.out.println();
            } catch (WordleGameException e) {
                log.println("Игровое исключение при вводе '" + userInput + "': " + e.getMessage());
                System.out.println(e.getMessage() + " Попробуйте ещё раз (попытка не сгорела).\n");
            }
        }

        if (game.isWon()) {
            System.out.println("Поздравляем! Вы отгадали слово!");
        } else {
            System.out.println("Попытки закончились. Вы проиграли.");
        }
        System.out.println("# загаданное слово: " + game.getAnswer());
    }
}