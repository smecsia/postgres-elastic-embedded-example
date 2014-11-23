# Postgresql and ElasticSearch embedded example

This example demonstrates the way to embed the PostgreSQL and ElasticSearch into your java application.
It aims to show how to use the [embedded PostgreSQL](https://github.com/yandex-qatools/postgresql-embedded),
[Elasticsearch](https://github.com/elasticsearch/elasticsearch) and [elasticsearch-river-jdbc](https://github.com/jprante/elasticsearch-river-jdbc)
in a simple way.

To see how it works, please take a look at the [test](https://github.com/smecsia/postgres-elastic-embedded-example/blob/master/src/test/java/ru/yandex/qatools/embed/service/EmbeddedPostgresServiceTest.java):

```java

    // start the services
    elastic.addToIndex(Post.class);

    // feed up the database
    List<Post> posts = new ArrayList<>();
    for (int i = 0; i < 100; ++i) {
        Post post = new Post();
        post.setTitle("Post title" + i);
        post.setText("Lorem ipsum dolor sit amet " + i);
        assertThat(post.saveIt(), is(true));
        posts.add(post);
    }
    
    // verify data is found by both SQL and ES queries
    assertThat(Post.findById(posts.get(0).getId()), not(nullValue()));
    assertThat("post must be found by full text query",
            elastic, should(fullTextFindAtLeast(Post.class, "dolor sit", 10))
                    .whileWaitingUntil(timeoutHasExpired(10000)));

```

