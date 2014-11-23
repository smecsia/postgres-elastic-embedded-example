package ru.yandex.qatools.embed.service;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.javalite.activejdbc.Model;

public class FullTextFindSubEntityMatcher<E extends Model> extends TypeSafeMatcher<FullTextSearchService> {

    private final Class entityClass;
    private final String searchQuery;
    private final MatchFunction<Boolean, FullTextSearchService> callable;

    private FullTextFindSubEntityMatcher(Class entityClass, String searchQuery,
                                         MatchFunction<Boolean, FullTextSearchService> callable) {
        this.entityClass = entityClass;
        this.searchQuery = searchQuery;
        this.callable = callable;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format(
                "Repository containing some %s found by query '%s'",
                entityClass.getSimpleName(), searchQuery));
    }

    @Override
    protected boolean matchesSafely(FullTextSearchService repo) {
        try {
            return callable.call(repo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static interface MatchFunction<R, P> {
        public R call(P arg);
    }

    public static <T extends Model> FullTextFindSubEntityMatcher<T> fullTextFindAny(final Class<T> entityClass, final String keyword) {
        return new FullTextFindSubEntityMatcher<T>(entityClass, keyword, new MatchFunction<Boolean, FullTextSearchService>() {
            @Override
            public Boolean call(FullTextSearchService repo) {
                return repo.search(entityClass, keyword).size() > 0;
            }
        });
    }

    public static <T extends Model> FullTextFindSubEntityMatcher<T> fullTextFindAtLeast(final Class<T> entityClass, final String keyword, final int count) {
        return new FullTextFindSubEntityMatcher<>(entityClass, keyword, new MatchFunction<Boolean, FullTextSearchService>() {
            @Override
            public Boolean call(FullTextSearchService repo) {
                return repo.search(entityClass, keyword).size() >= count;
            }
        });
    }

}