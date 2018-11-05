package red.man10.man10fightclubv2;

import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MFCdb {
    private final String sqlCreateFighterTable = "CREATE TABLE `mfc_data` (\n" +
            "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `uuid` varchar(40) NOT NULL COMMENT 'UUID',\n" +
            "  `name` varchar(40) NOT NULL DEFAULT '' COMMENT 'UUID',\n" +
            "  `totalget` int(11) DEFAULT NULL,\n" +
            "  `win` int(11) DEFAULT NULL,\n" +
            "  `lose` int(11) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;";

    private Man10FightClubV2 plugin;

    public MFCdb(Man10FightClubV2 plugin){
        this.plugin = plugin;
    }

    public boolean createTable(){
        return plugin.mysql.execute(sqlCreateFighterTable);
    }

    public String getTable(){
        return "mfc_data";
    }

    public int getPlayerWin(UUID id){
        int ret = 0;
        String sql = "select sum(win) from "+getTable()+" where uuid='" + id.toString()+"';";

        ResultSet rs = plugin.mysql.query(sql);
        try {
            while(rs.next())
            {
                ret = rs.getInt("sum(win)");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public int getPlayerLose(UUID id){
        int ret = 0;
        String sql = "select sum(lose) from "+getTable()+" where uuid='" + id.toString()+"';";

        ResultSet rs = plugin.mysql.query(sql);
        try {
            while(rs.next())
            {
                ret = rs.getInt("sum(lose)");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public int getPlayerTotalget(UUID id){
        int ret = 0;
        String sql = "select sum(totalget) from "+getTable()+" where uuid='" + id.toString()+"';";

        ResultSet rs = plugin.mysql.query(sql);
        try {
            while(rs.next())
            {
                ret = rs.getInt("sum(totalget)");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public double calcKilldata(int win,int death){
        BigDecimal b = new BigDecimal(win/death);
        b = b.setScale(2, RoundingMode.HALF_UP);
        return b.doubleValue();
    }

}
