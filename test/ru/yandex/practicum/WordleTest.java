package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static PrintWriter log;
    private static WordleDictionary fullDictionary;

    private WordleDictionary smallDictionary;
    private WordleGame game;

    @BeforeAll
    static void setUpAll() {
        log = new PrintWriter(System.out, true);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        fullDictionary = loader.loadDictionary("words_ru.txt");
    }

    @BeforeEach
    void setUp() {
        List<String> testWords = Arrays.asList("герой", "гонец", "город", "голос", "берег", "север");
        smallDictionary = new WordleDictionary(testWords, log);
        game = new WordleGame(smallDictionary, "герой", log);
    }

    @Test
    void shouldNormalizeWordCorrectly() {
        assertEquals("ежик", WordleDictionary.normalizeWord("  ЁЖИК "));
        assertEquals("герой", WordleDictionary.normalizeWord("ГеРоЙ"));
        assertEquals("ветер", WordleDictionary.normalizeWord("вЕтЁр"));
        assertEquals("", WordleDictionary.normalizeWord(null));
    }

    @Test
    void shouldValidateWordFormat() {
        assertTrue(WordleDictionary.isValidWordFormat("герой"));
        assertTrue(WordleDictionary.isValidWordFormat("пчёлы"));

        assertFalse(WordleDictionary.isValidWordFormat("дом"));
        assertFalse(WordleDictionary.isValidWordFormat("машина"));
        assertFalse(WordleDictionary.isValidWordFormat("hello"));
        assertFalse(WordleDictionary.isValidWordFormat("12345"));
        assertFalse(WordleDictionary.isValidWordFormat("аб-вг"));
        assertFalse(WordleDictionary.isValidWordFormat(null));
    }

    @Test
    void shouldCompareWordsCorrectly() {
        // Пример прямо из ТЗ: загадано "герой", введено "гонец" -> "+^-^-"
        assertEquals("+^-^-", WordleDictionary.compareWords("гонец", "герой"));
        assertEquals("+++++", WordleDictionary.compareWords("герой", "герой"));
        assertEquals("-----", WordleDictionary.compareWords("туман", "герой"));
        // Проверка повторяющихся букв: в "герой" одна 'е' (на 2-м месте) и одна 'р' (на 3-м месте)
        assertEquals("-+--^", WordleDictionary.compareWords("север", "герой"));
    }

    @Test
    void shouldLoadFullDictionaryProperly() {
        assertNotNull(fullDictionary);
        assertFalse(fullDictionary.isEmpty());
        for (String word : fullDictionary.getWords()) {
            assertEquals(WordleDictionary.WORD_LENGTH, word.length());
            assertFalse(word.contains("ё"));
            assertTrue(WordleDictionary.isValidWordFormat(word));
        }
    }

    @Test
    void shouldThrowExceptionOnInvalidWordAndNotDecrementSteps() {
        int initialSteps = game.getSteps();

        assertThrows(InvalidWordException.class, () -> game.makeStep(""));
        assertThrows(InvalidWordException.class, () -> game.makeStep("дом"));
        assertThrows(InvalidWordException.class, () -> game.makeStep("словоизшести"));
        assertThrows(InvalidWordException.class, () -> game.makeStep("apple"));
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeStep("абвгд"));

        assertEquals(initialSteps, game.getSteps());
    }

    @Test
    void shouldProcessValidStepsAndWinGame() throws WordleGameException {
        String hint1 = game.makeStep("гонец");
        assertEquals("+^-^-", hint1);
        assertEquals(5, game.getSteps());
        assertFalse(game.isWon());
        assertFalse(game.isGameOver());

        String hint2 = game.makeStep("ГЕРОЙ");
        assertEquals("+++++", hint2);
        assertEquals(4, game.getSteps());
        assertTrue(game.isWon());
        assertTrue(game.isGameOver());
    }

    @Test
    void shouldLoseGameAfterSixFailedAttempts() throws WordleGameException {
        for (int i = 0; i < WordleGame.MAX_STEPS; i++) {
            game.makeStep("гонец");
        }
        assertEquals(0, game.getSteps());
        assertFalse(game.isWon());
        assertTrue(game.isGameOver());

        assertThrows(WordleSystemException.class, () -> game.makeStep("герой"));
    }

    @Test
    void shouldFilterHintsAndFindCorrectAnswer() throws WordleGameException {
        game.makeStep("гонец");
        String hintWord = game.getHint();
        assertEquals("герой", hintWord);
    }

    @Test
    void shouldAllowComputerToPlayFullGameUsingOnlyHints() throws WordleGameException {
        WordleGame autoGame = new WordleGame(fullDictionary, log);

        while (!autoGame.isGameOver()) {
            String hintWord = autoGame.getHint();
            assertNotNull(hintWord);
            assertTrue(fullDictionary.contains(hintWord));
            autoGame.makeStep(hintWord);
        }

        assertTrue(autoGame.isGameOver());
        assertTrue(autoGame.getHistoryWords().size() <= WordleGame.MAX_STEPS);
    }
}
