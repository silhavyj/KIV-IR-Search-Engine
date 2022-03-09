package cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing;

import java.util.List;

public interface IPreprocessor {

    List<String> tokenize(final String text);
    String preprocess(final String token);
}
