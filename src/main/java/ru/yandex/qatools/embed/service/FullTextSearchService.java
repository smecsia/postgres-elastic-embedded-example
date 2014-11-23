package ru.yandex.qatools.embed.service;

import org.javalite.activejdbc.Model;
import ru.yandex.qatools.embed.beans.SearchResult;

import java.util.List;

/**
 * @author Ilya Sadykov
 */
public interface FullTextSearchService {
    List<SearchResult> search(Class<? extends Model> modelClass, String value);

    void addToIndex(Class<? extends Model> modelClass);
}
