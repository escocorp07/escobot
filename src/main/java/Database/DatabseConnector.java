package main.java.Database;

import arc.util.Log;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static main.java.BVars.*;

public class DatabseConnector {
    private static final DataSource dataSource = createDataSource();
    /**
     * Смотрите документацию postgresql
     */
    private static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(JDBC_URL);
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        return ds;
    }
    /**
     * Выполнить sql код асинхронно
     */
    private static <T> Optional<T> executeQueryAsync(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.apply(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            Log.err("Database query failed: " + sql, e);
            return Optional.empty();
        }
    }
    /**
     * Выполнить sql код как-то
     */
    private static boolean executeUpdate(String sql, ThrowingConsumer<PreparedStatement> parameterSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);
            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            Log.err("Database update failed: " + sql, e);
            return false;
        }
    }
    /**
     * Выполнить sql код как-то
     */
    private static <T> List<T> executeQueryList(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            Log.err("Database query failed: " + sql, e);
        }
        return results;
    }
    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
