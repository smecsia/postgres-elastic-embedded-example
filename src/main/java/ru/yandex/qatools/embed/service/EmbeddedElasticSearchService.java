package ru.yandex.qatools.embed.service;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.javalite.activejdbc.Model;
import org.postgresql.Driver;
import ru.yandex.qatools.embed.beans.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Example implementation of an embedded elasticsearch server.
 *
 * @author Felix Müller
 */
public class EmbeddedElasticSearchService extends EmbeddedService implements FullTextSearchService {
    private Node node;

    public EmbeddedElasticSearchService(String host, int port, String username, String password, String dbName, String dataDirectory, boolean enabled, int initTimeout) throws IOException {
        super(host, port, username, password, dbName, dataDirectory, enabled, initTimeout);
    }

    @Override
    protected void doStart() throws Exception {
        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("http.enabled", "false")
                .put("path.data", dataDirectory);
        this.node = nodeBuilder().local(true).settings(elasticsearchSettings.build()).node();
    }

    @Override
    protected void doStop() throws Exception {
        if (node != null) {
            node.stop();
            node.close();
            node = null;
        }
    }

    @Override
    public List<SearchResult> search(Class<? extends Model> modelClass, String value) {
        final List<SearchResult> results = new ArrayList<>();
        if (enabled) {
            final String collectionName = collectionName(modelClass);
            logger.debug(format("Searching for '%s' in collection '%s' ...", value, collectionName));
            final SearchResponse resp = search(collectionName, queryString(value));
            for (SearchHit hit : resp.getHits()) {
                results.add(new SearchResult(hit.getId(), hit.score(), hit.getSource()));
            }
            logger.debug(format("Search for '%s' in collection '%s' gave %d results...",
                    value, collectionName, results.size()));
        }
        return results;
    }

    @Override
    public void addToIndex(Class<? extends Model> modelClass) {
        try {
            logger.debug(format("Adding collection '%s' to the embedded ElasticSearch index...",
                    collectionName(modelClass)));
            indexQuery(modelClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to index collection", e);
        }
    }

    public Client getClient() {
        return node.client();
    }

    private SearchResponse search(String collectionName, QueryBuilder query) {
        final CountResponse count = count(collectionName, query);
        return getClient().prepareSearch().setTypes(collectionName)
                .setQuery(query)
                .setSize((int) count.getCount())
                .addFields("id")
                .execute()
                .actionGet();
    }

    private CountResponse count(String collectionName, QueryBuilder query) {
        return getClient().prepareCount().setTypes(collectionName)
                .setQuery(query)
                .execute()
                .actionGet();
    }

    private String collectionName(Class<? extends Model> modelClass) {
        try {
            return (String) modelClass.getMethod("getTableName").invoke(modelClass);
        } catch (Exception e) {
            return modelClass.getSimpleName().toLowerCase();
        }
    }

    private void indexQuery(Class<? extends Model> modelClass) throws IOException {
        if (enabled) {
            final String collectionName = collectionName(modelClass);
            final XContentBuilder config = jsonBuilder()
                    .startObject()
                    .field("type", "jdbc")
                        .startObject("jdbc")
                            .field("driver", Driver.class.getName())
                            .field("url", format("jdbc:postgresql://%s:%s/%s", host, port, dbName))
                            .field("user", username)
                            .field("password", password)
                            .field("index", "index")
                            .field("type", collectionName)
                            .field("strategy", "simple")
                            .field("sql", format("select id as _id, * from %s ", collectionName))
                        .endObject()
                        .startObject("index")
                            .field("index", collectionName)
                            .field("type", collectionName)
                            .field("bulk_size", "1000")
                            .field("bulk_timeout", "30")
                        .endObject()
                    .endObject();
            getClient().prepareIndex("_river", collectionName, "_meta").setSource(config)
                    .execute().actionGet(initTimeout);
        }
    }
}
