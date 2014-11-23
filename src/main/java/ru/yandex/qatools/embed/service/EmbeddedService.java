package ru.yandex.qatools.embed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static jodd.io.FileUtil.createTempDirectory;
import static jodd.io.FileUtil.deleteDir;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public abstract class EmbeddedService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final String host;
    protected final int port;
    protected final String dbName;
    protected final String username;
    protected final String password;
    protected final String dataDirectory;
    protected final int initTimeout;
    protected final boolean removeDataDir;
    protected final boolean enabled;
    protected volatile boolean stopped = false;

    public EmbeddedService(String host, int port,
                           String username, String password, String dbName,
                           String dataDirectory, boolean enabled, int initTimeout) throws IOException {
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.initTimeout = initTimeout;
        this.host = host;
        this.port = port;
        this.dbName = dbName;

        if (isEmpty(dataDirectory) || dataDirectory.equals("TMP")) {
            this.removeDataDir = true;
            this.dataDirectory = createTempDirectory("postgres", "data").getPath();
        } else {
            this.dataDirectory = dataDirectory;
            this.removeDataDir = false;
        }
    }


    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    public void start() {
        if (this.enabled) {
            try {
                logger.info("Starting the embedded service...");
                doStart();
            } catch (Exception e) {
                logger.error("Failed to start embedded service", e);
            }
        }
    }

    public void stop() {
        if (!stopped) {
            logger.info("Shutting down the embedded service...");
            stopped = true;
            try {
                doStop();
                if (removeDataDir) {
                    try {
                        deleteDir(new File(dataDirectory));
                    } catch (Exception e) {
                        logger.error("Failed to remove data dir", e);
                    }
                }
            }catch (Exception e){
                logger.error("Failed to stop service", e);
            }
        }

    }
}
