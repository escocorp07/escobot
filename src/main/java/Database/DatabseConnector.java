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
import static main.java.appeals.AppealStatus.parseStatus;

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
    public static Optional<Integer> createAppeal(String ip, String excuses, int ban_id) {
        return executeQueryAsync(
                "INSERT into appeals (ban_id, ip, excuses) VALUES (?,CAST(? AS INET),?)",
                stmt->{
                    stmt.setInt(1, ban_id);
                    stmt.setString(2, ip);
                    stmt.setString(3, excuses);
                },
                rs -> rs.getInt("id")
        );
    }
    public static boolean unbanPlayerByBanId(int ban_id) {
        return executeUpdate(
                "UPDATE bans SET is_active = false WHERE ban_id = ?",
                stmt->stmt.setInt(1, ban_id)
        );
    }
    public static boolean unbanPlayerByUUID(String uuid) {
        return executeUpdate(
                "UPDATE bans SET is_active = false WHERE player_id in (SELECT * FROM players WHERE uuid = ?)",
                stmt->stmt.setString(1, uuid)
        );
    }
    public static Optional<Appeal> getAppeal(int id) {
        return executeQueryAsync(
                "SELECT * FROM appeals WHERE id = ?"
                +" LIMIT 1",
                stmt->stmt.setInt(1, id),
                DatabseConnector::mapResultSetToAppeal
        );
    }
    private static Appeal mapResultSetToAppeal(ResultSet rs) throws SQLException {
        return new Appeal(
                rs.getString("ip"),
                rs.getInt("ban_id"),
                rs.getString("excuses"),
                parseStatus(rs.getString("status")).toString(),
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
        /**
         * Ожидает рассмотрения,
         * отклонена,
         * принята.
         * */
        String status;
        public Appeal(String ip, int ban_id, String excuses, String status, String comment) {
            this.ip=ip;
            this.ban_id=ban_id;
            this.excuses=excuses;
            this.status=status;
            this.admin_comment=comment;
        }
    }
}
