package main.java.Database;

import arc.util.Log;
import lombok.Getter;
import lombok.Setter;
import main.java.annotations.GenerateSet;
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

    public static void newAppeal() {

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
    public static Optional<Appeal> getAppeal(int id) {
        return executeQueryAsync(
                "SELECT * FROM appeals WHERE id = ?"
                +" LIMIT 1",
                stmt->stmt.setInt(1, id),
                DatabseConnector::mapResultSetToAppeal
        );
    }
    public static Optional<AppealResult> getAppealResult(int id) {
        return executeQueryAsync(
                "SELECT * FROM appealresult WHERE id = ?"
                        +" LIMIT 1",
                stmt->stmt.setInt(1, id),
                DatabseConnector::mapResultSetToAppealResult
        );
    }
    private static Appeal mapResultSetToAppeal(ResultSet rs) throws SQLException {
        return new Appeal(
                rs.getString("ip"),
                rs.getInt("ban_id"),
                rs.getString("excuses")
        );
    }
    private static AppealResult mapResultSetToAppealResult(ResultSet rs) throws SQLException {
        return new AppealResult(
                rs.getInt("id"),
                rs.getInt("appeal_id"),
                rs.getBoolean("result"),
                rs.getString("comment")
        );
    }
    @Getter
    @Setter
    public static class Appeal {
        String ip;
        int ban_id;
        String excuses;
        String admin_comment;
        boolean result; // принята ли.
        public Appeal(String ip, int ban_id, String excusest) {
            this.ip=ip;
            this.ban_id=ban_id;
            this.excuses=excuses;
        }
    }
    @Getter
    @Setter
    public static class AppealResult {
        int id;
        int appeal_id;
        boolean result;
        String admin_comment;
        public AppealResult(int id, int appeal_id, boolean result, String comment) {
            this.id=id;
            this.appeal_id=appeal_id;
            this.result=result;
            this.admin_comment=comment;
        }
    }
}
