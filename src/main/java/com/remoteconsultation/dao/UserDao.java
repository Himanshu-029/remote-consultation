package com.remoteconsultation.dao;

import com.remoteconsultation.config.DatabaseConnection;
import com.remoteconsultation.model.User;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class UserDao {

    private final DatabaseConnection databaseConnection;

    public UserDao(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    // Find a user by email
    public User findByEmail(String email) throws SQLException {

        String sql = """
                SELECT user_id,
                       full_name,
                       email,
                       password_hash,
                       phone,
                       role,
                       status,
                       created_at,
                       updated_at
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    User user = new User();

                    user.setUserId(resultSet.getLong("user_id"));
                    user.setFullName(resultSet.getString("full_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setPhone(resultSet.getString("phone"));
                    user.setRole(resultSet.getString("role"));
                    user.setStatus(resultSet.getString("status"));

                    if (resultSet.getTimestamp("created_at") != null) {
                        user.setCreatedAt(
                                resultSet.getTimestamp("created_at").toLocalDateTime()
                        );
                    }

                    if (resultSet.getTimestamp("updated_at") != null) {
                        user.setUpdatedAt(
                                resultSet.getTimestamp("updated_at").toLocalDateTime()
                        );
                    }

                    return user;
                }
            }
        }

        return null;
    }

    // Create a user using a new connection
    public Long createUser(User user) throws SQLException {

        try (Connection connection = databaseConnection.getConnection()) {
            return createUser(connection, user);
        }
    }

    // Create a user using an existing transaction connection
    public Long createUser(Connection connection, User user) throws SQLException {

        String sql = """
                INSERT INTO users
                    (full_name, email, password_hash, phone, role, status)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole());
            statement.setString(6, user.getStatus());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("User creation failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException("User ID could not be generated.");
    }


    public boolean isActiveProfessional(
            Connection connection,
            Long professionalId) throws SQLException {

        if (professionalId == null) {
            return false;
        }

        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE user_id = ?
                  AND role = 'PROFESSIONAL'
                  AND status = 'ACTIVE'
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, professionalId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        && resultSet.getInt(1) == 1;
            }
        }
    }

}