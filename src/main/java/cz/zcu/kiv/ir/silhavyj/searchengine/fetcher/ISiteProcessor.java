package cz.zcu.kiv.ir.silhavyj.searchengine.fetcher;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.Optional;

public interface ISiteProcessor {

    Optional<JSONObject> processSite(final Document document);
}
