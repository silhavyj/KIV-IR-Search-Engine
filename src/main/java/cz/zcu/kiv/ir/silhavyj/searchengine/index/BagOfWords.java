package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import java.util.*;

public class BagOfWords {

    private final Map<String, Integer> words;
    private int totalNumberOfWords;

    public BagOfWords() {
        words = new HashMap<>();
        totalNumberOfWords = 0;
    }

    public List<String> getWords() {
        return new LinkedList<>(words.keySet());
    }

    public void addAllWords(final Set<String> words) {
        for (final var word : words) {
            addWord(word);
        }
    }

    public void addAllWords(final BagOfWords document) {
        document.words.forEach((word, count) -> addWord(word, count));
    }

    public void addWord(String word, int count) {
        words.put(word, words.getOrDefault(word, 0) + count);
        totalNumberOfWords += count;
    }

    public void addWord(String word) {
        addWord(word, 1);
    }

    public int getTotalNumberOfWords() {
        return totalNumberOfWords;
    }

    public int getNumberOfUniqueWords() {
        return words.size();
    }

    public int getNumberOfOccurrences(final String word) {
        return words.getOrDefault(word, 0);
    }
}
