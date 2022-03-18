package cz.zcu.kiv.ir.silhavyj.searchengine.fetcher;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.Optional;

/***
 * @author Jakub Silhavy
 *
 * Interface defining functionality of a site processor.
 */
public interface ISiteProcessor {

    /***
     * Parses a document. It tries to find out whether there is an article
     * in the document or not. If there is an article, it will fetch it and
     * convert it into a JSON format.
     * @param document Document to be parsed
     * @return Optional of a JSONObject - it could be empty in case that no article has been found.
     */
    Optional<JSONObject> processSite(final Document document);
}
