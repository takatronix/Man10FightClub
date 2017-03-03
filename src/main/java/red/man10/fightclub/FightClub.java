package red.man10.fightclub;




import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import static red.man10.fightclub.FightClub.Status.*;


public final class FightClub extends JavaPlugin implements Listener {

    //   状態遷移 これらの状態遷移する
    public enum Status {
        Closed,                 //  開催前
        Entry,                  //  募集中
        Opened,                  // 予想の受付開
        Fighting,                //  対戦中
    }
    //      プレーヤ情報
    class  FighterInformation{
        UUID   uuid;
        String name;
        Boolean isDead;
    }
    //      購入者情報
    class  BuyerInformation{
        UUID    uuid;
        String name;
    }
    //      賭け情報
    class  BetInformation{
        UUID     buyerUUID;       //  購入者のUUID
        String buyerName;       //  購入者の名前
        int    fighterIndex;     //  プレーヤ情報
        double bet;             //  掛け金
    }
    double tax = 0;
    Status  currentStatus = Closed;

    //      対戦者リスト
    ArrayList<FighterInformation> filghters = new ArrayList<FighterInformation>();
    //      掛け金
    ArrayList<BetInformation> bets = new ArrayList<BetInformation>();
    //      購入者リスト (通知用)
    ArrayList<BuyerInformation> buyers = new ArrayList<BuyerInformation>();

    //////////////////////////////////
    //    公開API
    //////////////////////////////////

    //      対戦者登録
    public int registerFighter(UUID uuid,String name){

        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(int i = 0;i < filghters.size();i++){
            FighterInformation fighter = filghters.get(i);
            if(fighter.uuid == uuid){
                //  登録済みエラー表示
                return -1;
            }
        }
        //      追加
        FighterInformation playerInfo = new FighterInformation();
        playerInfo.uuid = uuid;
        playerInfo.name = name;
        playerInfo.isDead = false;
        filghters.add(playerInfo);
        return filghters.size();
    }

    //////////////////////////////////
    int getBuyerIndex(UUID uuid) {
        for(int i = 0;i < buyers.size();i++){
            if(buyers.get(i).uuid == uuid){
                return i;
            }
        }
        return -1;
    }
    //
    int getFighterIndex(UUID uuid) {
        for(int i = 0;i < filghters.size();i++){
            if(filghters.get(i).uuid == uuid){
                return i;
            }
        }
        return -1;
    }
    //      生存者数
    int getAliveFighterCount() {
        int ret = 0;
        for(int i = 0;i < filghters.size();i++){
            if(filghters.get(i).isDead == false){
                ret ++;
            }
        }
        return ret;
    }
    //      生存者数
    int getLastFighter() {
        int ret = 0;
        for(int i = 0;i < filghters.size();i++){
            if(filghters.get(i).isDead == false){
                return i;
            }
        }
        return -1;
    }


    double getFighterBetMoney(UUID uuid){


        int index = getFighterIndex(uuid);
        if(index == -1){
            return 0;
        }
        return getFighterBets(index);

    }
    //      購入された数
    int getFighterBetCount(UUID uuid){
        int index = getFighterIndex(uuid);
        if(index == -1){
            return 0;
        }

        int count = 0;
        for(int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            if (bet.fighterIndex == index){
                count ++;
            }
        }
        return count;
    }

    //////////////////////
    //      odds
    //////////////////////
    double getFighterOdds(UUID uuid){

        //      購入された金額
        double bet = getFighterBetMoney(uuid);
        double total = getTotalBets();
        if(bet == 0){
            return 1.0;
        }
        //  （賭けられたお金の合計 － 手数料）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - tax) / bet;
        return odds;
    }

    double getFighterBets(int fighterIndex){
        double totalBet = 0;
        for(int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            if (bet.fighterIndex == fighterIndex){
                totalBet += bet.bet;
            }
        }
        return totalBet;
    }

    ///////////////////////////////////
    //      トータル掛け金
    ///////////////////////////////////
    double getTotalBets(){
        double totalBet = 0;
        for(int i = 0;i < bets.size();i++){
            totalBet += bets.get(i).bet;
        }
        return totalBet;
    }
    //////////////////////////////////////////////
    //     プレイーやに賭ける 成功なら掛け金テーブルindex
    //////////////////////////////////////////////
    int  betFighter(UUID fighterUUID,double price,UUID buyerUUID,String buyerName){

        int index = getFighterIndex(fighterUUID);
        if(index == -1){
            return -1;
        }

        /////////////////////////////////////////
        //     同じ相手への購入ならbetをマージ
        /////////////////////////////////////////
        for(int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            //      同じ購入IDのみ
            if(bet.buyerUUID != buyerUUID){
                continue;
            }
            if(bet.fighterIndex == index){
                bets.get(i).bet += price;
                return i;
            }
        }

        BetInformation bet = new BetInformation();
        bet.bet = price;
        bet.fighterIndex = index;
        bet.buyerUUID = buyerUUID;
        bet.buyerName = buyerName;
        bets.add(bet);




        return bets.size();
    }

    //////////////////////////////////////////////
    //      ゲームを中断する  払い戻し後ステータスを Closedへ
    //////////////////////////////////////////////
    int cancelGame(Player p){

        //   払い戻し処理
        for (int i = 0;i < bets.size();i++) {
            BetInformation bet = bets.get(i);
            p.sendMessage("Return money to " + bet.buyerName + " $"+ bet.bet );

            this.deposit(bet.buyerUUID,bet.bet);
            Bukkit.getPlayer(bet.buyerName).sendMessage("ゲームがキャンセルされお金を$"+bet.bet+"返金しました。");
        }
        bets.clear();
        filghters.clear();
        buyers.clear();
        currentStatus = Closed;
        return 0;
    }
    //      募集開始
    public int openGame(){
        currentStatus = Opened;
        return 0;
    }
    //      ゲーム開始
    public int startGame(){
        currentStatus = Fighting;
        return 0;
    }

    //      対戦終了　winPlayer = -1 終了
    public int endGame(Player p,int fighterIndex){
        if (fighterIndex == -1){
            return cancelGame(p);
        }

        //  掛け金の計算
        double total  = getTotalBets();
        double winBet = getFighterBets(fighterIndex);


        //    オッズとは
        //  （賭けられたお金の合計 － 手数料）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - tax) / winBet;

        for (int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            if (bet.fighterIndex != fighterIndex){
                continue;
            }
            //      プレイヤーへの支払い金額
            double playerPayout = bet.bet * odds;
            //      プレイヤーへ支払い
            serverMessage(bet.buyerName+"は, 元金額:$" + bet.bet+"-> $"+playerPayout+"Odds x"+odds);

            //      通知
            this.deposit(bet.buyerUUID,playerPayout);

        }

        //      終了
        bets.clear();
        filghters.clear();
        buyers.clear();
        currentStatus = Closed;
        return 0;
    }


    public static Economy economy = null;
    private boolean setupEconomy() {

        serverMessage("setupEconomy");
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        serverMessage("setupEconomy2");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        serverMessage("setupEconomy3");
        economy = rsp.getProvider();
        serverMessage("economy ok");
        return economy != null;
    }

    /////////////////////////////////
    //     MySQL 設定値
    /////////////////////////////////
    String  mysql_ip;
    String  mysql_port;
    String  mysql_user;
    String  mysql_pass;
    String  mysql_db;

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////
    public void loadConfig(){
        this.reloadConfig();
        mysql_ip = this.getConfig().getString("server_config.mysql_ip");
        mysql_port = this.getConfig().getString("server_config.mysql_port");
        mysql_user = this.getConfig().getString("server_config.mysql_user");
        mysql_pass = this.getConfig().getString("server_config.mysql_pass");
        mysql_db = this.getConfig().getString("server_config.mysql_db");
        getLogger().info("Config loaded");
    }
    /////////////////////////////////
    //      起動
    /////////////////////////////////
    @Override
    public void onEnable() {
        getLogger().info("Enabled");
        this.saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents (this,this);

        //   テーブル作成
        createTables();

        //
        getCommand("mfc").setExecutor(new FightClubCommand(this));

        setupEconomy();
    }

    /////////////////////////////////
    //      終了
    /////////////////////////////////
    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }

    /*
    /////////////////////////////////
    //      コマンド処理
    /////////////////////////////////
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        return true;
    }
*/
    /////////////////////////////////
    //     ジョインイベント
    /////////////////////////////////
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        p.sendMessage(ChatColor.YELLOW  + "Man10 Fight Club System Started.");




    }

    double  getBalance(UUID uuid){
        return economy.getBalance(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }
    double  getBalance(Player p){
        return economy.getBalance(p);
    }
    Boolean  withdraw(UUID uuid, double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        EconomyResponse resp = economy.withdrawPlayer(p,money);
        if(resp.transactionSuccess()){
            return true;
        }
        return  false;
    }
    //      お金を入れる
    Boolean  deposit(UUID uuid,double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        EconomyResponse resp = economy.depositPlayer(p,money);
        if(resp.transactionSuccess()){
            serverMessage("振込成功" + p.getName() + " $"+ money);
            return true;
        }
        serverMessage("振込失敗" + p.getName() + " $"+ money);
        return  false;
    }
    /////////////////////////////////
    //      チャットイベント
    /////////////////////////////////
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        String message = e.getMessage();
        //p.sendMessage(ChatColor.YELLOW + message );


        p.sendMessage(String.format("You have %s", economy.format(economy.getBalance(p.getName()))));
        EconomyResponse r = economy.depositPlayer(p, 100);
        if(r.transactionSuccess()) {
            p.sendMessage(String.format("You were given %s and now have %s", economy.format(r.amount), economy.format(r.balance)));
        } else {
            p.sendMessage(String.format("An error occured: %s", r.errorMessage));
        }

        SidebarDisplay bar = new SidebarDisplay();
        bar.setShowPlayer(p);
        bar.setMainScoreboard(p);


        //return true;
        // command("say "+message);
      //  p.setScoreboard(setupScoreboard());
    }
    /////////////////////////////////
    //      デスイベント
    /////////////////////////////////
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {

       // getLogger().info("death :" +e.getDeathMessage());
       // command("say death"+e.getDeathMessage());
        //
        Player p = (Player)e.getEntity();


        //      死亡フラグを立てる
        int index = getFighterIndex(p.getUniqueId());
        if(index != -1){
            filghters.get(index).isDead = true;
            serverMessage("死亡!!!:"+p.getDisplayName());


            //      最後ならゲームを終了する
            if(getAliveFighterCount() <= 1){
                serverMessage("ゲーム終了！！！");

                int lastIndex = getLastFighter();
                endGame(p,lastIndex);
                return;

            }else{
                String s = p.getDisplayName() + "は死亡した！！";
                serverMessage(s);
                s = "生存者/プレーヤ= " + getAliveFighterCount() + "/" + filghters.size();
                serverMessage(s);
            }
        }
        String s = "生存者/プレーヤ= " + getAliveFighterCount() + "/" + filghters.size();
        serverMessage(s);
/*
        //      死亡フラグを立てる
        int index = getFighterIndex(p.getUniqueId().toString());
        filghters.get(index).isDead = true;
        serverMessage("死亡！！ "+p.getDisplayName());

        if(getAliveFighterCount() <= 1){
            serverMessage("ゲーム終了！！！");

        }else{
            String s = p.getDisplayName() + "は死亡した！！";
            serverMessage(s);
            s = "生存者/プレーヤ= " + getAliveFighterCount() + "/" + filghters.size();
            serverMessage(s);
        }
*/

    }
    /////////////////////////////////
    //      ダメージイベント
    /////////////////////////////////
    @EventHandler
    public void onHit(EntityDamageEvent e){



        serverMessage("damage :" +e.getDamage());

        Player p = (Player)e.getEntity();
        serverMessage("entity get");
        if(e.getEntity() instanceof Player)
        {
            serverMessage("instance player");
            command("say damage"+e.getDamage()+e.getCause()+e.getFinalDamage());

           // p.setScoreboard(board);

        }
        String s = "生存者/プレーヤ= " + getAliveFighterCount() + "/" + filghters.size();
        serverMessage(s);
        //command("say damage"+e.getDamage()+e.getCause()+e.getFinalDamage());
        //if (e.getEntity() instanceof Player){
          //  Player p = (Player)e;
       // }
    }
    //////////////////////////////////////////
    //        Chatテーブル
    //////////////////////////////////////////
    String sqlCrateChatLogTable = "CREATE TABLE `mfc_chat` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
            "  `server` varchar(100) DEFAULT NULL,\n" +
            "  `name` varchar(100) DEFAULT NULL,\n" +
            "  `message` varchar(400) DEFAULT NULL,\n" +
            "  `timestamp` varchar(50) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=104377 DEFAULT CHARSET=utf8;";

    void createTables(){
        executeSQL(sqlCrateChatLogTable);
    }

    ////////////////////////////////
    //      SQL実行
    ////////////////////////////////
    Boolean executeSQL(String sql){
        // getLogger().info("executing SQL" + sql);
        Connection conn;
        try {
            //      データベース作成
            Class.forName("com.mysql.jdbc.Driver");
            String databaseURL =  "jdbc:mysql://" + mysql_ip + "/" + mysql_db ;
            //getLogger().info(databaseURL);

            conn = DriverManager.getConnection(databaseURL,mysql_user,mysql_pass);
            Statement st = conn.createStatement();
            st.execute(sql);

            st.close();
            conn.close();
            //getLogger().info("SQL performed");
            return true;
        } catch(ClassNotFoundException e){
            getLogger().warning("Could not read driver");
        } catch(SQLException e){
            getLogger().warning("Database connection error");
        }
        return false;
    }

    //      ログメッセージ
    void log(String text){
        getLogger().info(text);
    }
    //     サーバーメッセージ
    void serverMessage(String text){
        //command("say "+text);
        Bukkit.getServer().broadcastMessage(text);
    }

    void titleMessage(Player p,String title,String subTitle){

    }

    //      コマンド実行　
    void command(String command){
        getServer().dispatchCommand(getServer().getConsoleSender(),command);
    }
}
