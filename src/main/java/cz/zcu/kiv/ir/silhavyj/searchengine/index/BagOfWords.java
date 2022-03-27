package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import java.util.*;

/***
 * @author Jakub Silhavy
 *
 * This class represents a bag of words. It is used to
 * store a document, so it can be used for ranking.
 */
public class BagOfWords {

    /*** Dictionary of terms occurring in a document. */
    private final Map<String, Integer> words;

    /*** Total number of words held in the class. */
    private int numberOfWords;

    /***
     * Creates an instance of the class.
     */
    public BagOfWords() {
        words = new HashMap<>();
        numberOfWords = 0;
    }

    /***
     * Returns a list of unique terms occurring in a document.
     * @return list of unique terms occurring in a document.
     * */
    public List<String> getWords() {
        return new LinkedList<>(words.keySet());
    }

    /***
     * Adds words (terms) into the bag of words representing a document.
     * @param words set of unique words (terms)
     */
    public void addAllWords(final Set<String> words) {
        for (final var word : words) {
            addWord(word);
        }
    }

    /***
     * Adds all terms of another document into the current one.
     * @param document another document
     */
    public void addAllWords(final BagOfWords document) {
        document.words.forEach(this::addWord);
    }

    /***
     * Adds a word (term) into the bag of words.
     * @param word word (term) to be added
     * @param count number of instances of the word
     */
    public void addWord(String word, int count) {
        words.put(word, words.getOrDefault(word, 0) + count);
        numberOfWords += count;
    }

    /***
     * Adds a word (term) into the bag of words.
     * @param word word (term) to be added
     */
    public void addWord(String word) {
        addWord(word, 1);
    }

    /***
     * Returns the total number of words stored in the bow.
     * @return total number of words
     */
    public int getNumberOfWords() {
        return numberOfWords;
    }

    /***
     * Returns whether a word exists in the collection or not.
     * @param word the word we're interested in
     * @return True, the word exists. False, otherwise.
     */
    public boolean contains(final String word) {
        return words.containsKey(word);
    }

    /***
     * Returns number of occurrences of a given word.
     * @param word given word (term)
     * @return number of occurrences of the word
     */
    public int getNumberOfOccurrences(final String word) {
        return words.getOrDefault(word, 0);
    }
}
