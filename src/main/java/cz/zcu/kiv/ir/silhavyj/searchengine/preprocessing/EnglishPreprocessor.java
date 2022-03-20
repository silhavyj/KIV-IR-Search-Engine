package cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing;

import cz.zcu.kiv.ir.silhavyj.searchengine.utils.IOUtils;

import opennlp.tools.stemmer.PorterStemmer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * @author Jakub Silhavy
 *
 * This class represents a prprocessor for the English language.
 * It splits a piece of text into tokens and applies stemming to them.
 */
public class EnglishPreprocessor implements IPreprocessor {

    /*** Regular expression used to split text into tokens - tokenization */
    private static final String regex = "(\\d+[,]\\d+[,]?(\\d+)?[,]?(\\d+)?)|(\\d+[:]\\d+)|(\\d+[.,](\\d+)?[.,]?(\\d+)?)|(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]|(?:(?<=^|[^\\p{L}\\d])'|'(?=[\\p{L}\\d]|$)|[\\p{L}\\d*])+|(<.*?>)";

    /*** Set of stop words of the English language */
    private final Set<String> stopWords;

    /*** Instance of PorterStemmer which was imported as an external library */
    private final PorterStemmer stemmer;

    /*** Regex pattern used to find matches in a given piece of text */
    private final Pattern pattern;

    /***
     * Creates an instance of the class.
     * @param stopWordsPath path to a file containing stop words
     */
    public EnglishPreprocessor(final String stopWordsPath) {
        stemmer = new PorterStemmer();
        stopWords = new HashSet<>();
        final var lines = IOUtils.readLines(stopWordsPath);
        pattern = Pattern.compile(regex);
        stopWords.addAll(lines);
    }

    /***
     * Preprocesses a piece of give text. The process involves
     * lowercasing, tokenizing, getting rid of stopwords, and stemming.
     * @param text piece of text (document) to be preprocessed
     * @return a list of preprocessed terms found in the document
     */
    @Override
    public List<String> tokenize(final String text) {
        final Matcher matcher = pattern.matcher(text);
        final List<String> tokens = new LinkedList<>();

        // Go through all tokens that match the regular expression.
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Retrieve the token.
            String token = text.substring(start, end);

            // Skip it if it's recognized as a stopword.
            if (!stopWords.contains(token)) {
                tokens.add(preprocess(token));
            }
        }
        return tokens;
    }

    /***
     * Preprocesses a given token. The token gets converted into lowercase
     * and using PorterStemmer, it gets stemmed as well.
     * @param token token to be preprocessed
     * @return preprocessed token (term)
     */
    @Override
    public String preprocess(final String token) {
        return stemmer.stem(token.toLowerCase());
    }
}
