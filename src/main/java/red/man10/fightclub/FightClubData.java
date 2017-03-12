package red.man10.fightclub;

import org.bukkit.Bukkit;
import red.man10.MySQLManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by takatronix on 2017/03/06.
 */
public class FightClubData {

    private final FightClub plugin;
    MySQLManager mysql = null;

    public FightClubData(FightClub plugin) {
        this.plugin = plugin;
        this.mysql = new MySQLManager(plugin,"MFC");
        //this.mysql.debugMode = true;

        mysql.execute(sqlCreateFightTable);
        mysql.execute(sqlCreateBetTable);
    }

    public int killCount(UUID id){

        int ret = -1;
        String sql = "select count(*) from mfc_fight where winner='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getInt("count(*)");
            }
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }
    public int deathCount(UUID id){

        int ret = -1;
        String sql = "select count(*) from mfc_fight where loser='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getInt("count(*)");
            }
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public double totalPrize(UUID id){
        double ret = 0;
        String sql = "select sum(prize) from mfc_fight where winner='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getDouble("sum(prize)");
            }
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public int getLatestId(){

        int ret = -1;
        String sql = "select * from mfc_fight order by id desc limit 1";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getInt("id");
            }
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public boolean updateFight(int fightId,int result,UUID winner,UUID loser,double duration){

        String sql = "update mfc_fight set"
                +" result="+result
                +" ,winner='"+winner.toString()
                +"' ,loser='"+loser.toString()
                +"' ,duration="+duration
                +" where id="+fightId
                +";";

        return mysql.execute(sql);

    }


    public int createFight(String stage,String kit,  UUID uuid1,UUID uuid2,double odds1,double odds2,int bet1,int bet2,double prize,double totalBet){

        String name1 = Bukkit.getPlayer(uuid1).getName();
        String name2 = Bukkit.getPlayer(uuid2).getName();

        boolean ret = mysql.execute("insert into mfc_fight values(0"
                +",'" + currentTime()
                +"','" + kit
                +"','" + stage
                +"','" + uuid1.toString()
                +"','" + uuid2.toString()
                +"','" + name1
                +"','" + name2
                +"'," + odds1
                +"," + odds2
                +"," + bet1
                +"," + bet2
                +"," + totalBet
                +"," + prize
                +",NULL,NULL,NULL,NULL"

                +");");

        if(ret == false){
            return -1;
        }

        return getLatestId();

    }

    public boolean createBet(int fightId,UUID uuid,double bet,boolean win,UUID fighterId,double odds,double profit){

        String name = Bukkit.getPlayer(uuid).getName();
        String fighterName = Bukkit.getPlayer(fighterId).getName();

        boolean ret = mysql.execute("insert into mfc_bet values(0,"+fightId
                +",'" + currentTime()
                +"','" + name
                +"','" + uuid.toString()
                +"'," + bet
                +"," + win
                +",'" + fighterId
                +"','" + fighterName
                +"'," + odds
                +"," + profit
                +");");


        return ret;
    }



    public String currentTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
        Bukkit.getLogger().info("datetime ");
        String currentTime = sdf.format(date);
       // Bukkit.getLogger().info(currentTime);
        return currentTime;
    }




    String sqlCreateFightTable = "CREATE TABLE `mfc_fight` (\n" +
            "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `datetime` datetime NOT NULL COMMENT '日付',\n" +
            "  `kit` varchar(40) NOT NULL DEFAULT '',\n" +
            "  `stage` varchar(40) NOT NULL DEFAULT '',\n" +
            "  `uuid1` varchar(40) NOT NULL COMMENT 'UUID',\n" +
            "  `uuid2` varchar(40) NOT NULL DEFAULT '' COMMENT 'UUID',\n" +
            "  `player1` varchar(40) NOT NULL DEFAULT '' COMMENT 'UUID',\n" +
            "  `player2` varchar(40) NOT NULL DEFAULT '' COMMENT 'UUID',\n" +
            "  `odds1` double DEFAULT NULL,\n" +
            "  `odds2` double DEFAULT NULL,\n" +
            "  `bet1` int(11) DEFAULT NULL,\n" +
            "  `bet2` int(11) DEFAULT NULL,\n" +
            "  `totalbet` double DEFAULT NULL,\n" +
            "  `prize` double DEFAULT NULL,\n" +
            "  `result` int(11) DEFAULT NULL COMMENT '0:Cancel 1:player1 2:player2',\n" +
            "  `winner` varchar(40) DEFAULT '' COMMENT 'UUID',\n" +
            "  `loser` varchar(40) DEFAULT NULL COMMENT 'UUID',\n" +
            "  `duration` float DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;";

    String sqlCreateBetTable = "CREATE TABLE `mfc_bet` (\n" +
            "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `fight_id` int(11) DEFAULT NULL,\n" +
            "  `datetime` datetime NOT NULL COMMENT '日付',\n" +
            "  `name` varchar(40) NOT NULL DEFAULT '' COMMENT 'ユーザー名',\n" +
            "  `uuid` varchar(40) NOT NULL DEFAULT '' COMMENT 'UUID',\n" +
            "  `bet` double DEFAULT NULL,\n" +
            "  `win` tinyint(1) DEFAULT NULL COMMENT '勝敗',\n" +
            "  `fighter_uuid` varchar(40) NOT NULL DEFAULT '' COMMENT 'UUID',\n" +
            "  `fighter_name` varchar(40) NOT NULL DEFAULT '' COMMENT 'ユーザー名',\n" +
            "  `odds` double DEFAULT NULL COMMENT 'オッズ',\n" +
            "  `profit` double DEFAULT NULL COMMENT '収益',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

}
