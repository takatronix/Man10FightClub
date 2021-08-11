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
    }

    public String getBetTable(){
        String ret = "mfc_bet";
        if(plugin.mode == FightClub.MFCModes.Pro){
            ret = "mfcpro_bet";
        }
        return  ret;
    }
    public String getFightTable(){
        String ret = "mfc_fight";
        if(plugin.mode == FightClub.MFCModes.Pro){
            ret = "mfcpro_fight";
        }
        return  ret;
    }
    public String getPlayerTable(){
        String ret = "mfc_player";
        if(plugin.mode == FightClub.MFCModes.Pro){
            ret = "mfcpro_player";
        }
        return  ret;
    }

    public int killCount(UUID id){

        int ret = -1;
//        String sql = "select count(*) from mfc_fight where winner='" + id.toString()+"';";
        String sql = "select count(*) from "+getFightTable()+" where winner='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        if(rs == null){
          //  Bukkit.getServer().broadcastMessage("query error");
            return ret;
        }
        try
        {
            while(rs.next())
            {
                ret = rs.getInt("count(*)");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }
    public int deathCount(UUID id){

        int ret = -1;
        String sql = "select count(*) from "+getFightTable()+" where loser='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getInt("count(*)");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public double totalPrize(UUID id){
        double ret = 0;
        String sql = "select sum(prize) from "+getFightTable()+" where winner='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getDouble("sum(prize)");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }

    public double totalBetted(UUID id){
        double ret = 0;
        String sql = "select sum(totalBet) from "+getFightTable()+" where winner='" + id.toString()+"' or loser='" + id.toString()+ "'";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getDouble("sum(totalBet)");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }


    public int getLatestId(){

        int ret = -1;
        String sql = "select * from "+getFightTable()+" order by id desc limit 1";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getInt("id");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return ret;
    }


    public boolean createScore(UUID id,int kill,int death,double totalPrize,String table){

        String name = Bukkit.getOfflinePlayer(id).getName();


        boolean ret = mysql.execute("insert into "+table+"(0"
                +",'" + name
                +"','" + id.toString()
                +"'," + kill
                +"," + death
                +"," + totalPrize
                +",'" + currentTime()
                +"');");

        return  ret;
    }



    public boolean updateFight(int fightId,int result,UUID winner,UUID loser,double duration){

        String sql = "update "+getFightTable()+" set"
                +" result="+result
                +" ,winner='"+winner.toString()
                +"' ,loser='"+loser.toString()
                +"' ,duration="+duration
                +" where id="+fightId
                +";";

        return mysql.execute(sql);

    }


    /**
     * ゲーム作成
     * @param stage
     * @param kit
     * @param uuid1
     * @param uuid2
     * @param odds1
     * @param odds2
     * @param bet1
     * @param bet2
     * @param prize
     * @param totalBet
     * @return
     */
    public int createFight(String stage,String kit,  UUID uuid1,UUID uuid2,double odds1,double odds2,int bet1,int bet2,double prize,double totalBet){

        String name1 = Bukkit.getOfflinePlayer(uuid1).getName();
        String name2 = Bukkit.getOfflinePlayer(uuid2).getName();

        boolean ret = mysql.execute("insert into "+getFightTable()+" values(0"
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


        String name = Bukkit.getOfflinePlayer(uuid).getName();
        String fighterName = Bukkit.getOfflinePlayer(fighterId).getName();

        boolean ret = mysql.execute("insert into "+getBetTable()+" values(0,"+fightId
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

    public boolean savePlayerData(UUID uuid,int kill,int death,double kdr,double prize, double betted,int score){

        String mcid = Bukkit.getOfflinePlayer(uuid).getName();

        boolean ret;

         ret = mysql.execute("delete from "+getPlayerTable()+" where uuid='"+uuid+"'");

         ret = mysql.execute("insert into "+getPlayerTable()+" values(0"
                +",now()"
                +",'" + uuid
                +"'," + kill
                +"," + death
                +"," + kdr
                +"," + prize
                +"," + betted
                +",'" + mcid
                +"'," + score
                +");");


        return ret;
    }



    public String currentTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
        String currentTime = sdf.format(date);
        return currentTime;
    }
}
