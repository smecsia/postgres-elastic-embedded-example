package ru.yandex.qatools.embed.service;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.IOException;

import static ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.*;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.PRODUCTION;

/**
 * Example implementation of an embedded postgres server
 */
public class EmbeddedPostgresService extends EmbeddedService {

    PostgresProcess process;

    public EmbeddedPostgresService(String host, int port, String username, String password, String dbName, String dataDirectory, boolean enabled, int initTimeout) throws IOException {
        super(host, port, username, password, dbName, dataDirectory, enabled, initTimeout);
    }

    @Override
    public void doStart() throws Exception {
        PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
        final PostgresConfig configDb;
        configDb = new PostgresConfig(
                PRODUCTION, new Net(host, port), new Storage(dbName, dataDirectory),
                new Timeout(initTimeout), new Credentials(username, password));
        PostgresExecutable exec = runtime.prepare(configDb);
        process = exec.start();
    }

    @Override
    public void doStop() throws Exception {
        if (process != null) {
            process.stop();
            process = null;
        }
    }
}
