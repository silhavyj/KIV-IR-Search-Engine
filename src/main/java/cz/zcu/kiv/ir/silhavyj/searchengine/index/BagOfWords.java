package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import java.util.HashMap;
import java.util.Map;

public class BagOfWords {

    private final Map<String, Integer> words;
    private int totalNumberOfWords;

    public BagOfWords() {
        words = new HashMap<>();
        totalNumberOfWords = 0;
    }

    public void addWord(String word) {
        words.put(word, words.getOrDefault(word, 0) + 1);
        totalNumberOfWords++;
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
