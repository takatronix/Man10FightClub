package red.man10;

/**
 * Created by takatronix on 2017/03/05.
 */

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySQLFunc {
    String HOST = null;
    String DB = null;
    String USER = null;
    String PASS = null;
    String PORT = null;
    private Connection con = null;

    public MySQLFunc(String host, String db, String user, String pass,String port) {
        this.HOST = host;
        this.DB = db;
        this.USER = user;
        this.PASS = pass;
        this.PORT = port;
    }

    public Connection open() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.con = DriverManager.getConnection("jdbc:mysql://" + this.HOST + ":" + this.PORT +"/" + this.DB + "?useSSL=false", this.USER, this.PASS );
            return this.con;
        } catch (SQLException var2) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not connect to MySQL server, error code: " + var2.getErrorCode());
        } catch (ClassNotFoundException var3) {
            Bukkit.getLogger().log(Level.SEVERE, "JDBC driver was not found in this machine.");
        }

        return this.con;
    }

    public boolean checkConnection() {
        return this.con != null;
    }

    public void close(Connection c) {
        c = null;
    }

    public Connection getCon() {
        return this.con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
}
