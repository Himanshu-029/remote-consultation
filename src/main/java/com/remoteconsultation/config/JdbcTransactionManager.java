
package com.remoteconsultation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class JdbcTransactionManager {

    private static final Logger logger =
            LoggerFactory.getLogger(JdbcTransactionManager.class);

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();

        try {
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }

            throw e;
        }
    }

    public void commit(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException(
                    "Cannot commit: connection is unavailable.");
        }

        connection.commit();
    }

    public void rollback(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            logger.error("Failed to roll back the database transaction.", e);
        }
    }

    public void close(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close the database connection.", e);
        }
    }
}
