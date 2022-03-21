package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;

import java.util.Set;

/***
 * @author Jakub Silhavy
 *
 * Interface defining functionality of a query parser.
 */
public interface IQueryParser {

    /***
     * Returns the error message.
     * @return the error message
     */
    String getErrorMessage();

    /***
     * Checks if the query passed in as a string is valid or not.
     * @param query query written in an infix notation
     * @return True, if the query is valid. False, otherwise.
     */
    boolean isValidQuery(final String query);

    /***
     * Performs a search
     * @param index index in which the search will be performed
     * @param query query the user entered (string, infix notation)
     * @return Linked list of Documents representing the result of the search.
     * @throws IllegalArgumentException if the user entered an invalid query
     */
    Document search(final IIndex index, String query) throws IllegalArgumentException;

    /***
     * Returns relevant words of a query
     * @return relevant words of a query as a Set (uniqueness)
     */
    Set<String> getRelevantWords();
}
