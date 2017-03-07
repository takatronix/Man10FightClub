package red.man10.fightclub;

import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.glow.GlowAPI;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import red.man10.MySQLManager;
import red.man10.SidebarDisplay;
import red.man10.VaultManager;

import java.io.File;
import java.util.*;

import static org.bukkit.boss.BarFlag.CREATE_FOG;
import static org.bukkit.boss.BarStyle.SEGMENTED_20;
import static red.man10.fightclub.FightClub.Status.*;


public final class FightClub extends JavaPlugin implements Listener {

    FightClubGUI gui = new FightClubGUI(this);

    VaultManager vault = null;
    MySQLManager mysql = null;


    double      prize = 0.1;
    double      tax   = 0.;

    //   状態遷移 これらの状態遷移する
    public enum Status {
        Closed,                 //  開催前
        Entry,                  //  募集中
        Opened,                 //  予想の受付開
        Fighting,               //  対戦中
    }
    //      ファイター情報
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

    Status  currentStatus = Entry;

    //      対戦まちリスト
    ArrayList<FighterInformation> waiters = new ArrayList<FighterInformation>();

    //      対戦者リスト
    ArrayList<FighterInformation> fighters = new ArrayList<FighterInformation>();
    //      掛け金
    ArrayList<BetInformation> bets = new ArrayList<BetInformation>();


    ////////////////////////////////
    //       対戦者登録
    ////////////////////////////////
    public int registerFighter(UUID uuid,String name){

        resetEnetryTimer();

        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(int i = 0;i < waiters.size();i++){
            FighterInformation fighter = waiters.get(i);
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
        waiters.add(playerInfo);

        updateSidebar();
        return waiters.size();
    }

    //
    int getFighterIndex(UUID uuid) {
        for(int i = 0;i < fighters.size();i++){
            if(fighters.get(i).uuid == uuid){
                return i;
            }
        }
        return -1;
    }
    //      生存者数
    int getAliveFighterCount() {
        int ret = 0;
        for(int i = 0;i < fighters.size();i++){
            if(fighters.get(i).isDead == false){
                ret ++;
            }
        }
        return ret;
    }
    //      生存者数
    int getLastFighter() {
        int ret = 0;
        for(int i = 0;i < fighters.size();i++){
            if(fighters.get(i).isDead == false){
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
        double odds = (total - getCost()) / bet;
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


    Boolean canBet(UUID buyerUUID){

        /*
        //          ファイターは登録できません
        for(int i=0;i< fighters.size();i++){
            if(fighters.get(i).uuid == buyerUUID){
                serverMessage( "§d八百長防止のため、選手はベットすることはできません");
                return false;
            }
        }
*/
        return true;
    }

    //////////////////////////////////////////////
    //     プレイーやに賭ける 成功なら掛け金テーブルindex
    //////////////////////////////////////////////
    int  betFighter(UUID fighterUUID,double price,UUID buyerUUID,String buyerName){

        int index = getFighterIndex(fighterUUID);
        if(index == -1){
            return -1;
        }

        if(canBet(buyerUUID) == false){
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

        resetBetTimer();
        return bets.size();
    }

    void disableGlow(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            GlowAPI.setGlowing(player,false,Bukkit.getOnlinePlayers());
        }
    }

    //////////////////////////////////////////////
    //      ゲームを中断する  払い戻し後ステータスを Closedへ
    //////////////////////////////////////////////
    int cancelGame(){

        disableGlow();
        //   払い戻し処理
        for (int i = 0;i < bets.size();i++) {
            BetInformation bet = bets.get(i);
           //p.sendMessage("Return money to " + bet.buyerName + " $"+ bet.bet );

            vault.deposit(bet.buyerUUID,bet.bet);
            Bukkit.getPlayer(bet.buyerName).sendMessage("ゲームがキャンセルされお金を$"+bet.bet+"返金しました。");
        }

        if(fighters.size() >= 2){
            //      服装をバックアップ
           // command("mkit pop "+fighters.get(0).name);
           // command("mkit pop "+fighters.get(1).name);
        }

        startEntry();

        return 0;
    }

    public boolean canStartGame(){

        double    limit = 10000;
        //      双方にベットされているか
        if(getFighterBetMoney(fighters.get(0).uuid) < limit){
            return false;
        }
        //      双方にベットされているか
        if(getFighterBetMoney(fighters.get(1).uuid) < limit){
            return false;
        }
        return true;
    }
    //      募集開始
    public int startGame(){

        if(fighters.size() < 2){
            serverMessage("二人以上いないと開催できませんキャンセルします");
            cancelGame();
            return 0;
        }


        if(canStartGame() == false){
            serverMessage("ベットされた金額が足らないため試合をキャンセルします");
            cancelGame();
            return 0;
        }



        currentStatus = Fighting;
        serverMessage("ファイト！！！！");
        fightTimer = 180;

        command("man10 usertp "+ fighters.get(0).name + " player1");
        command("man10 usertp "+ fighters.get(1).name + " player1");

        updateSidebar();
        return 0;
    }
    public int startEntry(){
        bets.clear();
        fighters.clear();
        waiters.clear();
        currentStatus = Entry;
        updateSidebar();
        return 0;
    }

    String   selectedKit = "";
    //      ゲーム開始
    public boolean openGame(){

        if(waiters.size() < 2){
            serverMessage("二人以上いないと開催できません");
            return false;
        }

        //      シャッフルする
        Collections.shuffle(waiters);


        fighters.clear();

        int    max = 2;         //  最大マッチ数
        for(int i=0;i< waiters.size();i++){
            FighterInformation f = waiters.get(i);
            Player p = Bukkit.getPlayer(f.uuid);
            if(p.isOnline() ){
                fighters.add(f);
                if(fighters.size() >= max){
                    break;
                }
            }
        }

        if (fighters.size() < 2){
            serverMessage("選手が足らないためMFC開始できません");
            fighters.clear();
            return false;
        }


        //      キットの自動選択処理
        List<String> kits = listKits();
        Collections.shuffle(kits);
        selectedKit = kits.get(0);

        if(currentStatus == Entry){
/*
        //      服装をバックアップ
        command("mkit push "+fighters.get(0).name);
        command("mkit push "+fighters.get(1).name);

*/
        }

        //      キットを選択


        command("mkit set "+fighters.get(0).name + " " + selectedKit);
        command("mkit set "+fighters.get(1).name + " " + selectedKit);



        resetBetTimer();

        //      ファイト開始
        currentStatus = Opened;
        updateSidebar();


        return true;
    }

    //      経費
    public double getCost(){
        return getTax() + getPrize();
    }
    public double getTax(){
        return  getTotalBets() * tax;
    }

    //      賞金
    public double getPrize(){
        if(fighters.size() < 2) {
            return 0;
        }
        double t = getTotalBets() * prize;
        double f1 = getFighterBetMoney(fighters.get(0).uuid);
        double f2 = getFighterBetMoney(fighters.get(1).uuid);
        if(t > f1){
            t = f1;
        }
        if(t > f2){
            t = f2;
        }
        return t;
    }

    //      対戦終了　winPlayer = -1 終了
    public int endGame(Player p,int fighterIndex){

        if (fighterIndex == -1){
            return cancelGame();
        }

        disableGlow();


        //  掛け金の計算
        double total  = getTotalBets();
        double winBet = getFighterBets(fighterIndex);

        //    オッズとは
        //  （賭けられたお金の合計 － 経費）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - getCost()) / winBet;

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
            vault.deposit(bet.buyerUUID,playerPayout);

        }

        //      終了

        startEntry();
        updateSidebar();

        return 0;
    }

    int      entryTimer = 0;
    int      betTimer = 0;
    int      fightTimer = 0;

    public void resetEnetryTimer(){
        entryTimer = 60;
    }
    public void resetBetTimer(){
        betTimer = 90;
    }


    public void onTimer(){
      //  log("onTimer");
        if (currentStatus == Entry) {
            if(waiters.size() >= 2){
                entryTimer --;
                if(entryTimer <= 0){
                    openGame();
                }
            }
            updateSidebar();
        }
        if (currentStatus == Opened) {
            //             serverMessage("timer opened" + betTimer);
            betTimer--;

            if(betTimer <= 0){
                startGame();
            }
            updateSidebar();
        }

        if (currentStatus == Fighting) {
            fightTimer--;
            if(fightTimer <= 0){
                serverMessage("タイムアウト！！！　");
                cancelGame();
            }

            updateSidebar();
        }
    }


    /////////////////////////////////
    //      起動
    /////////////////////////////////
    @Override
    public void onEnable() {
        getLogger().info("Enabled");
        this.saveDefaultConfig();




        getServer().getPluginManager().registerEvents (this,this);


        //
       getCommand("mfc").setExecutor(new FightClubCommand(this));


        vault = new VaultManager(this);
        updateSidebar();
       // mysql = new MySQLManager(this,"MFC");



        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                onTimer();
            }
        }, 0, 20);

    }


    /////////////////////////////////
    //      終了
    /////////////////////////////////
    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }


    /////////////////////////////////
    //     ジョインイベント
    /////////////////////////////////
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        p.sendMessage(ChatColor.YELLOW  + "Man10 Fight Club System Started.");
        updateSidebar();
    }

    /////////////////////////////////
    //      チャットイベント
    /////////////////////////////////
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        String message = e.getMessage();
      //  GlowAPI.setGlowing(e.getPlayer(), GlowAPI.Color.AQUA, Bukkit.getOnlinePlayers())
        // ;


        org.bukkit.boss.BossBar bossBar =  Bukkit.getServer().createBossBar("title", BarColor.RED,SEGMENTED_20,CREATE_FOG);
    //    bossBar.set

    }
    /////////////////////////////////
    //      デスイベント
    /////////////////////////////////
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {

        if(currentStatus != Fighting){
            return;
        }

        Player p = (Player)e.getEntity();

        //      死亡フラグを立てる
        int index = getFighterIndex(p.getUniqueId());
        if(index != -1){
            fighters.get(index).isDead = true;
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
                s = "生存者/プレーヤ= " + getAliveFighterCount() + "/" + fighters.size();
                serverMessage(s);
            }
            updateSidebar();

        }


    }
    /////////////////////////////////
    //      ダメージイベント
    /////////////////////////////////
    @EventHandler
    public void onHit(EntityDamageEvent e){

        if(currentStatus != Fighting){
            return;
        }
       // serverMessage("damage :" +e.getDamage());

        if(e.getEntity() instanceof Player)
        {
            Player p = (Player)e.getEntity();
            double d = e.getDamage();
            if(d == 0){
                return;
            }
            String dam = String.format("%.2f",d);
           // serverMessage(p.getName()+"は、"+dam+"ダメージ を受けた！！！");
            updateSidebar();
        }

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

    public void guiBetMenu(Player p){

        //p.sendMessage("GUIを開いた");
        if(currentStatus == Opened){
            //
            gui.betMenu(p);
        }else{
            p.sendMessage("現在は投票できません");
        }
        updateSidebar();
    }

    //      キット
    public List<String> listKits() {

        List<String> ret  = new ArrayList<String>();

        File folder = new File(Bukkit.getServer().getPluginManager().getPlugin("Man10Kit").getDataFolder(), File.separator + "Kits");

        File[] files = folder.listFiles();  // (a)
        for (File f : files) {
            if (f.isFile()){  // (c)
                String filename = f.getName();

                if(filename.substring(0,1).equalsIgnoreCase(".")){
                    continue;
                }

                int point = filename.lastIndexOf(".");
                if (point != -1) {
                    String kitName =  filename.substring(0, point);
                    ret.add(kitName);
                }
             //   p.sendMessage(filename);
            }
        }

        return ret;
    }

    @EventHandler
    public void clickSignEvent(PlayerInteractEvent e) {

        if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
            Sign s = (Sign) e.getClickedBlock().getState();
            if (s.getLine(0).equalsIgnoreCase("[MFC]") == false){
                return;
            }
            if (s.getLine(1).equalsIgnoreCase("BET")) {


                guiBetMenu(e.getPlayer());

                return;
            }
            if (s.getLine(1).equalsIgnoreCase("Register")) {
                registerFighter(e.getPlayer().getUniqueId(),e.getPlayer().getName());
                return;
            }
            if (s.getLine(1).equalsIgnoreCase("Entry")) {
                startEntry();
                return;
            }
            if (s.getLine(1).equalsIgnoreCase("Cancel")) {
                cancelGame();
                return;
            }
            if (s.getLine(1).equalsIgnoreCase("Open")) {
                openGame();
                return;
            }
            if (s.getLine(1).equalsIgnoreCase("Fight")) {
                startGame();
                return;
            }


        }

    }


    @EventHandler
    public void clickItem(InventoryClickEvent e) {

        if(currentStatus == Entry || currentStatus == Opened){
            gui.clickItem(e);
        }

    }

    //      サイドバー
    FightClubSideBar sideBar = new FightClubSideBar(this);
    void updateSidebar(){
        sideBar.show();
    }



    //      コマンド実行　
    void command(String command){
        getServer().dispatchCommand(getServer().getConsoleSender(),command);
    }
}
