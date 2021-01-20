package Lesson_6.Server;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:mainDB.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) throws SQLException {
        String sql = String.format("SELECT nickname FROM main where login = '%s' and password = '%s'",
                login, pass);

        ResultSet rs = stmt.executeQuery(sql);

        if(rs.next()) {
            return rs.getString(1);
        }
        return null;
    }


    public static Boolean RegistrationNewUser(String login, String pass, String nickname) {
        String sql = String.format("INSERT INTO main (login, password, nickname) VALUES ('%s','%s','%s')",
                login, pass, nickname);

        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static String getBlacklist(String currNick) throws SQLException {
        String sql = String.format("SELECT Blocked FROM blacklist WHERE user LIKE '%s'", currNick);

        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            return rs.getString(1);
        }
        else {
            createBlacklist(currNick, "");
        }
        return "";
    }

    public static void createBlacklist(String currNick, String listBlocked) throws SQLException {
        String sql = String.format("INSERT INTO blacklist (user, Blocked) VALUES ('%s','%s')",
                currNick, listBlocked);
        stmt.executeUpdate(sql);
    }

    public static void updateBlacklist(String currNick, String listBlocked) throws SQLException {
        String sql = String.format("UPDATE blacklist SET Blocked = '%s' WHERE user = '%s'",
                listBlocked, currNick);
        stmt.executeUpdate(sql);
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
