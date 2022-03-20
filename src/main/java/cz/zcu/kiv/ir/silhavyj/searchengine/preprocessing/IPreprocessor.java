package cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing;

import java.util.List;

/***
 * @author Jakub Silhavy
 *
 * Interface defining functionality of a preprocessor.
 */
public interface IPreprocessor {

    /***
     * Preprocesses a piece of give text. The process involves
     * lowercasing, tokenizing, getting rid of stopwords, and stemming.
     * @param text piece of text (document) to be preprocessed
     * @return a list of preprocessed terms found in the document
     */
    List<String> tokenize(final String text);

    /***
     * Preprocesses a given token. The token gets converted into lowercase
     * and using PorterStemmer, it gets stemmed as well.
     * @param token token to be preprocessed
     * @return preprocessed token (term)
     */
    String preprocess(final String token);
}
