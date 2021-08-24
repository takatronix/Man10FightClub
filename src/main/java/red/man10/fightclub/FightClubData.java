package red.man10.fightclub;

import org.bukkit.Bukkit;
import red.man10.MySQLManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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


    /**
     * Kill数取得
     * @param id
     * @return
     */
    public int killCount(UUID id){

        int ret = -1;
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

    /**
     * Death数を履歴から取得
     * @param id
     * @return
     */
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
    public double maxPrize(UUID id){
        double ret = 0;
        String sql = "select max(prize) from "+getFightTable()+" where winner='" + id.toString()+"';";

        ResultSet rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                ret = rs.getDouble("max(prize)");
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

    /**
     *
     * @param fightId
     * @param result
     * @param winner
     * @param loser
     * @param duration
     * @return
     */
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
                +",now()"
                +",'" + kit
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
                +",now()"
                +",'" + name
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

    /**
     * プレイヤーデータはMFCとMFC_Proをわけて保存
     * @param uuid
     * @param kill
     * @param death
     * @param kdr
     * @param total_prize
     * @param max_prize
     * @param betted
     * @param score
     * @return
     */
    public boolean savePlayerData(UUID uuid,int kill,int death,double kdr,double total_prize,double max_prize, double betted,int score){

        String mcid = Bukkit.getOfflinePlayer(uuid).getName();

       boolean isPro = false;
        if(plugin.mode == FightClub.MFCModes.Pro){
            isPro = true;
        }

        deletePlayerData(isPro,uuid);

        boolean ret;

         ret = mysql.execute("insert into "+getPlayerTable()+" values(0"
                +",now()"
                +",'" + uuid
                +"'," + kill
                +"," + death
                +"," + kdr
                +"," + total_prize
                 +"," + max_prize
                +"," + betted
                +",'" + mcid
                +"'," + score
                +");");


        return ret;
    }


    /**
     *
     * @param sql
     * @return
     */
    public ArrayList<PlayerInformation> getPlayerData(String sql){
        var list= new ArrayList<PlayerInformation>() ;
        var rs = mysql.query(sql);
        try
        {
            while(rs.next())
            {
                var pi = new PlayerInformation();
                pi.uuid = UUID.fromString(rs.getString("uuid"));
                pi.kill = rs.getInt("kill");
                pi.death = rs.getInt("death");
                pi.total_prize = rs.getDouble("totalprize");
                pi.max_prize = rs.getDouble("maxprize");
                pi.betted = rs.getDouble("betted");
                pi.name = rs.getString("mcid");
                pi.datetime = rs.getDate("datetime");
                list.add(pi);
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
        }

        return list;
    }

    /**
     * プレイヤーデータ削除
     * @param uuid
     * @return
     */
    public boolean deletePlayerData(boolean isPro ,UUID uuid){
        var tableName = "mfc_player";
        if(isPro)
            tableName = "mfcpro_player";

        var ret = mysql.execute("delete from "+ tableName+ " where uuid='"+uuid+"'");
        if(ret == false){
            plugin.log("savePlayerData delete error");
        }
        return ret;
    }

    /**
     * プレイヤーデータ取得 NULLならデータなし
     * @param uuid
     * @return
     */
    public PlayerInformation getPlayerData(boolean isPro,UUID uuid){
        var tableName = "mfc_player";
        if(isPro)
            tableName = "mfcpro_player";

        var sql = "select * from "+tableName+" where uuid='"+uuid+"'";
        var result =  getPlayerData(sql);
        if(result.size() <= 0) {
            var pi =  new PlayerInformation();
            pi.uuid = uuid;
            pi.name = Bukkit.getPlayer(uuid).getName();
            return pi;
        }
        return result.get(0);
    }

    /**
     * 総合獲得賞金ランキング
     * @param limit
     * @param offset
     * @return
     */
    public ArrayList<PlayerInformation> getPrizeRanking(boolean isPro,int limit,int offset){
        var tableName = "mfc_player";
        if(isPro)
            tableName = "mfcpro_player";

        var sql = "select * from "+tableName+" order by totalprize desc limit " + limit + " offset " + offset;
        var result =  getPlayerData(sql);
        return result;
    }

    /**
     * KDRランキング
     * @param limit
     * @param offset
     * @return
     */
    public ArrayList<PlayerInformation> getKDRRanking(boolean isPro,int limit,int offset){
        var tableName = "mfc_player";
        if(isPro)
            tableName = "mfcpro_player";

        var sql = "select *,`kill`+death from " + tableName + " where `kill`+death >=" + plugin.newbiePlayableCount + " order by kdr desc limit "+ limit +" offset " + offset;
        var result =  getPlayerData(sql);
        return result;
    }
    /**
     * Score Ranking
     * @param limit
     * @param offset
     * @return
     */
    public ArrayList<PlayerInformation> getScoreRanking(boolean isPro,int limit,int offset){
        var tableName = "mfc_player";
        if(isPro)
            tableName = "mfcpro_player";

        var sql = "select * from "+tableName+" order by score desc limit " + limit + " offset " + offset;
        var result =  getPlayerData(sql);
        return result;
    }

    /**
     * 一試合での最高獲得
     * @param limit
     * @param offset
     * @return
     */
    public ArrayList<PlayerInformation> getMaxPrize(boolean isPro,int limit,int offset){
        var tableName = "mfc_player";
        if(isPro)
            tableName = "mfcpro_player";

        var sql = "select * from "+tableName+" order by maxprize desc limit " + limit + " offset " + offset;
        var result =  getPlayerData(sql);
        return result;
    }

/*
    public String currentTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
        String currentTime = sdf.format(date);
        return currentTime;
    }
    */

}
