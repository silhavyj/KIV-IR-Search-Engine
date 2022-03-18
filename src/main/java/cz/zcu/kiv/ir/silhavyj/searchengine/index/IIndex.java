package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.IPreprocessor;
import javafx.beans.property.IntegerProperty;

import java.util.Set;

/***
 * @author Jakub Silhavy
 *
 * Interface defining functionality of an inverted index.
 */
public interface IIndex {

    /***
     * Adds a document into the index.
     * @param term term that has been found in a document
     * @param documentIndex index of the document where the term's been found
     * @param filePath path to the document
     */
    void addDocument(final String term, int documentIndex, final String filePath);

    /***
     * Returns a linked list of all documents.
     * @return head of a linked list of all documents.
     */
    Document getAllDocumentIndexes();

    /***
     * Returns a filepath of a given document
     * @param documentIndex index of a document
     * @return filepath of a give document
     * @throws IllegalArgumentException if the document has not been indexed yet
     */
    String getFilePath(int documentIndex) throws IllegalArgumentException;

    /***
     * Returns a linked list (row in the index) of a given term
     * @param term given term
     * @return linked list (row in the index) of a given term
     */
    Document getDocuments(final String term);

    /***
     * Indexes a document given as a piece of text
     * @param text content of a document to be indexed
     * @param filePath filepath of the document
     * @return True, if the document's been indexed successfully. False, otherwise.
     */
    boolean index(final String text, final String filePath);

    /***
     * Returns the instance of a preprocessor used within the index.
     * @return preprocessor of the index
     */
    IPreprocessor getPreprocessor();

    /***
     * Return the total number of documents.
     * @return number of documents that have been indexed
     */
    int getDocumentCount();

    /***
     * Return document count property that is
     * used to update the view.
     * @return document count property
     */
    IntegerProperty documentCountProperty();

    /***
     * Return the total number of terms.
     * @return number of terms that were encountered during indexing
     */
    int getTermCount();

    /***
     * Return term count property that is
     * used to update the view.
     * @return term count property
     */
    IntegerProperty termCountProperty();

    /***
     * Returns number of tokens occurred during indexing.
     * @return total number of tokens
     */
    int getTokenCount();

    /***
     * Return token count property that is
     * used to update the view.
     * @return token count property
     */
    IntegerProperty tokenCountProperty();

    /***
     * Calculates TF-IDF for a given document and a unique
     * set of words (query).
     * @param index index of a document
     * @param relevantTerms relevant words of a query
     * @return value of TF-IDF
     */
    double calculateTF_IDF(int index, Set<String> relevantTerms);

    /***
     * Calculates cosine similarity for a given document and relevant words of a query.
     * @param index index of a document
     * @param relevantTerms relevant words of a query
     * @return cosine similarity
     */
    double calculateCosineSimilarity(int index, Set<String> relevantTerms);
}
