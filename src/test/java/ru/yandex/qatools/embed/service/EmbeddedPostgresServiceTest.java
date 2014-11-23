package ru.yandex.qatools.embed.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.embed.beans.Post;

import java.util.ArrayList;
import java.util.List;

import static jodd.io.FileUtil.createTempDirectory;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.embed.service.FullTextFindSubEntityMatcher.fullTextFindAtLeast;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.TimeoutWaiter.timeoutHasExpired;

public class EmbeddedPostgresServiceTest {
    public static final int INIT_TIMEOUT = 10000;
    public static final String DB_NAME = "test";
    public static final String HOST = "localhost";
    public static final int PORT = 5429;
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "123";
    public static final int POOL_SIZE = 10;
    EmbeddedPostgresService postgres;
    EmbeddedElasticSearchService elastic;
    Database database;

    @Before
    public void setUp() throws Exception {
        final String dbDir = createTempDirectory("postgresdb", "").getAbsolutePath();
        final String idxDir = createTempDirectory("elastic", "").getAbsolutePath();
        postgres = new EmbeddedPostgresService(HOST, PORT, USERNAME, PASSWORD, DB_NAME, dbDir, true, INIT_TIMEOUT);
        postgres.start();
        elastic = new EmbeddedElasticSearchService(HOST, PORT, USERNAME, PASSWORD, DB_NAME, idxDir, true, INIT_TIMEOUT);
        elastic.start();
        database = new Database(HOST, PORT, DB_NAME, USERNAME, PASSWORD, POOL_SIZE);
        database.connect();
        elastic.addToIndex(Post.class);
    }

    @Test
    public void testSaveFindPost() throws Exception {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            Post post = new Post();
            post.setTitle("Post title" + i);
            post.setText("Lorem ipsum dolor sit amet " + i);
            assertThat(post.saveIt(), is(true));
            posts.add(post);
        }
        assertThat(Post.findById(posts.get(0).getId()), not(nullValue()));
        assertThat("post must be found by full text query",
                elastic, should(fullTextFindAtLeast(Post.class, "dolor sit", 10))
                        .whileWaitingUntil(timeoutHasExpired(10000)));
    }

    @After
    public void tearDown() throws Exception {
        database.disconnect();
        postgres.stop();
        elastic.stop();
    }

}