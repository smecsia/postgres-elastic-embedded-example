package ru.yandex.qatools.embed.beans;

import java.util.Map;

/**
 * @author smecsia
 */
public class SearchResult {

    final String id;
    final float score;
    final Map<String, Object> attrs;

    public SearchResult(String id, float score, Map<String, Object> attrs) {
        this.id = id;
        this.score = score;
        this.attrs = attrs;
    }

    public String getId() {
        return id;
    }

    public float getScore() {
        return score;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }
}
