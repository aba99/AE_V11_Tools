package com.automic.api;

import com.uc4.communication.Connection;
import com.uc4.communication.requests.CreateSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Factory for connections to the Automation Engine
 */
public class AeConnectionFactory {

    private final String serverName;
    private final int port;
    private final static Logger LOGGER = LoggerFactory.getLogger(AeConnection.class);

    private static final char LANGUAGE = 'E';

    private static final int CONNECTION_TIMEOUT_MILLIS = 10000;
    private static final Properties AE_PROPERTIES = new Properties();

    static {
        AE_PROPERTIES.setProperty("DIRECT", "YES");
    }

    public AeConnectionFactory(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public AeConnection createConnection(AeCredentials credentials) {
        LOGGER.info("uc4.jar version:" + com.uc4.UCVersion.VERSION);
        LOGGER.info("Connecting to connection @ " + serverName + ":" + port);
        LOGGER.info("Client:  " + credentials.getClient());
        LOGGER.info("User:  " + credentials.getUser());
        LOGGER.info("Department:  " + credentials.getDepartment());

        try {
            Connection connection = Connection.open(serverName, port, AE_PROPERTIES);
            try {
                connection.setTraceListener(new LoggingTraceListener());
                connection.setTimeout(CONNECTION_TIMEOUT_MILLIS);
                LOGGER.info("Connection to AE {}:{} successfully established", serverName, port);

                CreateSession login = connection.login(
                        credentials.getClient(),
                        credentials.getUser(),
                        credentials.getDepartment(),
                        credentials.getPassword(),
                        LANGUAGE);

                if (!login.isLoginSuccessful()) {
                    throw new LoginFailedException(String.format("Login at client %s with user %s/%s failed",
                            credentials.getClient(),
                            credentials.getUser(),
                            credentials.getDepartment()));
                }
                LOGGER.info("Successfully logged in at client {} with user {}/{}",
                        credentials.getClient(),
                        credentials.getUser(),
                        credentials.getDepartment());
                return new AeConnection(connection);
            }
            catch (Exception e) {
                connection.close();
                throw e;
            }
        } catch (Exception e) {
            throw new AeException(
                    String.format("Unable to create connection to the Automation Engine using credentials %s",
                            credentials),
                    e);
        }
    }

    /**
     * Returns the server name (hostname) of the Automation Engine
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Returns the Automation Engine port
     */
    public int getPort() {
        return port;
    }
}
