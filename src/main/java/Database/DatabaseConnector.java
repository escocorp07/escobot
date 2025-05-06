package main.java.Database;

import arc.graphics.Color;
import arc.util.Log;
import arc.util.Strings;
import lombok.Getter;
import lombok.Setter;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static main.java.BVars.*;
import static main.java.appeals.AppealStatus.parseStatus;

public class DatabaseConnector {
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
    public static Optional<String> createAppeal(String ip, String excuses, int ban_id) {
        return executeQueryAsync(
                "INSERT into appeals (ban_id, ip, excuses) VALUES (?,CAST(? AS INET),?) RETURNING id",
                stmt->{
                    stmt.setInt(1, ban_id);
                    stmt.setString(2, ip);
                    stmt.setString(3, excuses);
                },
                rs -> rs.getString("id")
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
    public static Optional<Appeal> getAppeal(String id) {
        return executeQueryAsync(
                "SELECT * FROM appeals WHERE id = ?",
                stmt->stmt.setString(1, id),
                DatabaseConnector::mapResultSetToAppeal
        );
    }
    public static Optional<Appeal> getAppealByMessage(String id) {
        return executeQueryAsync(
                "SELECT * FROM appeals WHERE message_id = ?",
                stmt->stmt.setString(1, id),
                DatabaseConnector::mapResultSetToAppeal
        );
    }
    public static boolean setAppealMessageId(String message_id, String appeal_id) {
        return executeUpdate(
                "UPDATE appeals SET message_id = ? WHERE id = ?",
                stmt->{
                    stmt.setString(1, message_id);
                    stmt.setString(2, appeal_id);
                }
        );
    }
    public static boolean setAppealComment(String comment, String appeal_id) {
        return executeUpdate(
                "UPDATE appeals SET comment = ? WHERE id = ?",
                stmt->{
                    stmt.setString(1, comment);
                    stmt.setString(2, appeal_id);
                }
        );
    }
    public static boolean setAppealStatus(String appeal_id, String status) {
        return executeUpdate(
                "UPDATE appeals SET status = ? WHERE id = ?",
                stmt->{
                    stmt.setString(1, status);
                    stmt.setString(2, appeal_id);
                }
        );
    }
    public static Optional<PlayerData> getPlayerById(int id) {
        return executeQueryAsync(
                "SELECT * FROM Players WHERE Id = ?",
                stmt -> stmt.setInt(1, id),
                DatabaseConnector::mapResultSetToPlayer
        );
    }
    private static Appeal mapResultSetToAppeal(ResultSet rs) throws SQLException {
        return new Appeal(
                rs.getInt("id"),
                rs.getString("ip"),
                rs.getInt("ban_id"),
                rs.getString("excuses"),
                parseStatus(rs.getString("status")).toString(),
                rs.getString("comment"),
                rs.getString("message_id")
        );
    }

    @Getter
    @Setter
    public static class Appeal {
        String id;
        String ip;
        int ban_id;
        String excuses;
        String admin_comment;
        String discord_message;
        /**
         * Ожидает рассмотрения,
         * отклонена,
         * принята.
         * */
        String status;
        public Appeal(String id, String ip, int ban_id, String excuses, String status, String comment, String discord_message) {
            this.ip=ip;
            this.ban_id=ban_id;
            this.excuses=excuses;
            this.status=status;
            this.admin_comment=comment;
            this.discord_message=discord_message;
            this.id=id;
        }
    }
    private static String mapResultSetToPlayerData(ResultSet rs) throws SQLException {
        return Strings.format("Trace info of player ID @\n" +
                        "last name: @\n" +
                        "[white]custom name: @\n" +
                        "[white]UUID: @\n" +
                        "custom level: @\n" +
                        "[white]use custom level: @\n" +
                        "rank color: @\n" +
                        "experience: @\n" +
                        "wins: @\n" +
                        "losses: @\n" +
                        "blocks placed: @\n" +
                        "blocks broken: @\n" +
                        "waves survived: @\n" +
                        "playtime: @\n" +
                        "last ip: @\n" +
                        "first join: @\n" +
                        "social credit score: @\n" +
                        "color: @\n" +
                        "mobile: @\n" +
                        "locale: @\n" +
                        "discord id: @\n" +
                        "IPs: @\n" +
                        "Names: @\n",
                rs.getString("id"),
                rs.getString("last_name"),
                rs.getString("custom_name"),
                rs.getString("uuid"),
                rs.getString("custom_level"),
                rs.getString("use_custom_level"),
                rs.getString("rank_color"),
                rs.getString("experience"),
                rs.getString("wins"),
                rs.getString("loses"),
                rs.getString("blocks_placed"),
                rs.getString("blocks_broken"),
                rs.getString("waves_survived"),
                rs.getString("playtime"),
                rs.getString("last_ip"),
                rs.getString("first_join"),
                rs.getString("social_credit_score"),
                rs.getString("color"),
                rs.getString("mobile"),
                rs.getString("locale"),
                rs.getString("discord_id"),
                rs.getString("ips"),
                rs.getString("names")
        );
    }
    private static PlayerData mapResultSetToPlayer(ResultSet rs) throws SQLException {
        return new PlayerData(
                rs.getInt("Id"),
                rs.getString("Uuid"),
                rs.getString("Last_ip"),
                rs.getString("Last_name"),
                rs.getString("Custom_name"),
                rs.getString("Custom_level"),
                rs.getString("Rank_color"),
                rs.getLong("Experience"),
                rs.getInt("Wins"),
                rs.getInt("Loses"),
                rs.getInt("Blocks_placed"),
                rs.getInt("Blocks_broken"),
                rs.getInt("Waves_survived"),
                rs.getLong("Playtime"),
                rs.getBoolean("Use_custom_level"),
                rs.getTimestamp("First_join").toInstant(),
                rs.getBoolean("Mobile"),
                rs.getString("locale"),
                Color.valueOf(rs.getString("color"))
        );
    }
    public static class PlayerData {
        private final int id;
        private final String uuid;
        private final String lastIP;
        private final String lastName;
        private final String customName;
        private final String customLevel;
        private final String rankColor;
        private final long experience;
        private final int wins;
        private final int loses;
        private final int blocksPlaced;
        private final int blocksBroken;
        private final int wavesSurvived;
        private final long playtime;
        private final boolean useCustomLevel;
        private final Instant firstJoinTime;
        private final boolean isMobile;
        private final String locale;
        private final Color color;

        PlayerData(int id, String uuid, String lastIP, String lastName, String customName,
                   String customLevel, String rankColor, long experience, int wins, int loses,
                   int blocksPlaced, int blocksBroken, int wavesSurvived, long playtime,
                   boolean useCustomLevel, Instant firstJoinTime, boolean isMobile, String locale, Color color) {
            this.id = id;
            this.uuid = uuid;
            this.lastIP = lastIP;
            this.lastName = lastName;
            this.customName = customName;
            this.customLevel = customLevel;
            this.rankColor = rankColor;
            this.experience = experience;
            this.wins = wins;
            this.loses = loses;
            this.blocksPlaced = blocksPlaced;
            this.blocksBroken = blocksBroken;
            this.wavesSurvived = wavesSurvived;
            this.playtime = playtime;
            this.useCustomLevel = useCustomLevel;
            this.firstJoinTime = firstJoinTime;
            this.isMobile = isMobile;
            this.locale = locale;
            this.color = color;
        }

        public int getLevel() {
            return (int) Math.floor(0.5 * (Math.sqrt(0.08 * experience + 1) - 1));
        }

        public int getExperienceForLevel(int level) {
            return (int) (((2 * level + 1) * (2 * level + 1) - 1) / 0.08);
        }

        public String getDisplayLevel() {
            return useCustomLevel && customLevel != null ? customLevel : String.valueOf(getLevel());
        }

        public String getName() {
            return customName == null || customName.length() == 0 ? lastName : customName;
        }

        public String getRankColor() {
            return rankColor == null ? "#ffffffff" : rankColor;
        }

        public String getDisplayName() {
            return String.format("[%s]<[green]%s[%s]> [#%s]%s", getRankColor(), getDisplayLevel(), getRankColor(), color, getName());
        }

        // Геттеры
        public int getId() {
            return id;
        }

        public String getUuid() {
            return uuid;
        }

        public String getLastIP() {
            return lastIP;
        }

        public String getLastName() {
            return lastName;
        }

        public String getCustomName() {
            return customName;
        }

        public String getCustomLevel() {
            return customLevel;
        }

        public long getExperience() {
            return experience;
        }

        public int getWins() {
            return wins;
        }

        public int getLoses() {
            return loses;
        }

        public int getBlocksPlaced() {
            return blocksPlaced;
        }

        public int getBlocksBroken() {
            return blocksBroken;
        }

        public int getWavesSurvived() {
            return wavesSurvived;
        }

        public long getPlaytime() {
            return playtime;
        }

        public boolean getUseCustomLevel() {
            return useCustomLevel;
        }

        public Instant getFirstJoinTime() {
            return firstJoinTime;
        }

        public boolean getIsMobile() {
            return isMobile;
        }

        public String getLocale() {
            return locale;
        }

        public Color getColor() {
            return color;
        }
    }
}
