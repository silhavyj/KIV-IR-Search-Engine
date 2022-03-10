package cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing;

import cz.zcu.kiv.ir.silhavyj.searchengine.utils.IOUtils;

import opennlp.tools.stemmer.PorterStemmer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnglishPreprocessor implements IPreprocessor {

    private static final String regex = "(\\d+[,]\\d+[,]?(\\d+)?[,]?(\\d+)?)|(\\d+[:]\\d+)|(\\d+[.,](\\d+)?[.,]?(\\d+)?)|(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]|(?:(?<=^|[^\\p{L}\\d])'|'(?=[\\p{L}\\d]|$)|[\\p{L}\\d\\*])+|(<.*?>)";

    private final Set<String> stopWords;
    private final PorterStemmer stemmer;

    public EnglishPreprocessor(final String stopWordsPath) {
        stemmer = new PorterStemmer();
        stopWords = new HashSet<>();
        final var lines = IOUtils.readLines(stopWordsPath);
        for (final var line : lines) {
            stopWords.add(line);
        }
    }

    @Override
    public List<String> tokenize(final String text) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(text);
        final List<String> tokens = new LinkedList<>();

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String token = text.substring(start, end);

            if (!stopWords.contains(token)) {
                tokens.add(preprocess(token));
            }
        }
        return tokens;
    }

    @Override
    public String preprocess(final String token) {
        return stemmer.stem(token.toLowerCase());
    }
}
