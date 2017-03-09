package red.man10.fightclub;

import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.*;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.inventivetalent.glow.GlowAPI;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import red.man10.LifeBar;
import red.man10.MySQLManager;
import red.man10.SidebarDisplay;
import red.man10.VaultManager;

import java.io.File;
import java.util.*;
import java.util.Vector;

import static org.bukkit.boss.BarFlag.CREATE_FOG;
import static org.bukkit.boss.BarStyle.SEGMENTED_20;
import static red.man10.fightclub.FightClub.Status.*;


public final class FightClub extends JavaPlugin implements Listener {

    FightClubGUI gui = new FightClubGUI(this);
    LifeBar lifebar = new LifeBar(this);

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
    //     プレイヤー情報
    class  PlayerInformation{
        UUID        uuid;
        String      name;
        Location    returnLoc;          //  戻る場所
        Boolean     isDead;
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
    ArrayList<PlayerInformation> waiters = new ArrayList<PlayerInformation>();

    //      対戦者リスト
    ArrayList<PlayerInformation> fighters = new ArrayList<PlayerInformation>();

    //      観戦者リスト
    ArrayList<PlayerInformation> spectators = new ArrayList<PlayerInformation>();

    //      掛け金
    ArrayList<BetInformation> bets = new ArrayList<BetInformation>();

    public int unregisterFighter(UUID uuid){
        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(int i = 0;i < waiters.size();i++){
            PlayerInformation fighter = waiters.get(i);
            if(fighter.uuid == uuid){

                waiters.remove(i);
                updateSidebar();;
                return 0;
            }
        }
        return 1;
    }
    ////////////////////////////////
    //       対戦者登録
    ////////////////////////////////
    public int registerFighter(UUID uuid,String name){

        resetEnetryTimer();

        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(PlayerInformation waiter : this.waiters){
            if(waiter.uuid == uuid){
                //  登録済みエラー表示
                return -1;
            }
        }

        ////////////////////////////////////
        //      観戦者登録されてたらエラー
        ////////////////////////////////////
        for(PlayerInformation p : this.spectators){
            if(p.uuid == uuid){
                //  登録済みエラー表示
                return -1;
            }
        }


        //      追加
        PlayerInformation playerInfo = new PlayerInformation();
        playerInfo.uuid = uuid;
        playerInfo.name = name;
        playerInfo.isDead = false;
        playerInfo.returnLoc = Bukkit.getPlayer(uuid).getLocation();
        waiters.add(playerInfo);

        updateSidebar();
        return waiters.size();
    }

    //      観戦者
    public int registerSpectator(UUID uuid){

        ////////////////////////////////////////
        //      ファイター登録登録されてたらエラー
        ////////////////////////////////////////
        for(PlayerInformation waiter : waiters){
            if(waiter.uuid == uuid){
                //  登録済みエラー表示
                return -1;
            }
        }

        ////////////////////////////////////////
        //      ファイター登録登録されてたらエラー
        ////////////////////////////////////////
        for(PlayerInformation p : spectators){
            if(p.uuid == uuid){
                //  登録済みエラー表示
                return -1;
            }
        }

        //      追加
        PlayerInformation playerInfo = new PlayerInformation();
        playerInfo.uuid = uuid;
        playerInfo.isDead = false;
        playerInfo.returnLoc = Bukkit.getPlayer(uuid).getLocation();
        spectators.add(playerInfo);

        //      スポンへ移動
        Player p = Bukkit.getPlayer(uuid);
        tp(p,selectedArena,"spawn");
        p.setGameMode(GameMode.SPECTATOR);

   //     updateSidebar();
        return spectators.size();
    }

    public int unregisterSpectator(UUID uuid){

        PlayerInformation inf = null;
        for(PlayerInformation p : spectators){
            if(p.uuid == uuid){
                inf = p;
                break;
            }
        }

        //      もとの場所にもどす
        Player player = Bukkit.getPlayer(uuid);
        player.teleport(inf.returnLoc);
        player.setGameMode(GameMode.SURVIVAL);

        return 0;
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
        bets.clear();

        resetBetTimer();
        resetEnetryTimer();
        resetFightTimer();

        if(fighters.size() >= 2){
            //      服装をバックアップ
           // command("mkit pop "+fighters.get(0).name);
           // command("mkit pop "+fighters.get(1).name);
        }
        fighters.clear();

        //     ファイター移動
        tpf(selectedArena,"spawn");
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


        showLifeBarToAll();
        currentStatus = Fighting;
        serverMessage("ファイト！！！！");
        fightTimer = 180;

        //      キットを選択


        command("mkit set "+fighters.get(0).name + " " + selectedKit);
        command("mkit set "+fighters.get(1).name + " " + selectedKit);

        tp(Bukkit.getPlayer(fighters.get(0).uuid),selectedArena,"player1");
        tp(Bukkit.getPlayer(fighters.get(1).uuid),selectedArena,"player2");

        updateSidebar();
        return 0;
    }
    public int startEntry(){
        entryTimer = 90;
        bets.clear();
        fighters.clear();
        currentStatus = Entry;
        closeLifeBar();
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
        if(currentStatus == Opened){
            cancelGame();
            return false;
        }


        //      シャッフルする
        Collections.shuffle(waiters);


        fighters.clear();

        int    max = 2;         //  最大マッチ数
        for(int i=0;i< waiters.size();i++){
            PlayerInformation f = waiters.get(i);
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

        //      アリーナの自動選択
        Collections.shuffle(arenas);
        selectedArena = arenas.get(0);

        //     選手全員をアリーナへ移動
        tpf(selectedArena,"spawn");


        Player f0 = Bukkit.getPlayer(fighters.get(0).uuid);
        Player f1 = Bukkit.getPlayer(fighters.get(1).uuid);

        //      init bar
        lifebar.setRname(f0.getName());
        lifebar.setBname(f1.getName());
        lifebar.setVisible(true);


        if(currentStatus == Entry){
/*
        //      服装をバックアップ
        command("mkit push "+fighters.get(0).name);
        command("mkit push "+fighters.get(1).name);

*/
        }



     //   command("man10 tpuser "+ fighters.get(0).name + " player1");
       // command("man10 tpuser "+ fighters.get(1).name + " player2");
        Player player1 = Bukkit.getPlayer(fighters.get(0).uuid);
        Player player2 = Bukkit.getPlayer(fighters.get(0).uuid);

     //   tp(player1,selectedArena,"player1");
     //   tp(player2,selectedArena,"player2");

        resetBetTimer();

        //      ファイト開始
        currentStatus = Opened;
        updateSidebar();


        return true;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

        //      本人登録
        if(cmd.getName().equalsIgnoreCase("mfcr")){
            Player p = (Player)sender;
            registerFighter(p.getUniqueId(),p.getName());
            return true;
        }
        return false;
        // コマンドが実行されなかった場合は、falseを返して当メソッドを抜ける。
    }
    boolean CheckFreezed(Player player){
        return false;
        /*
        if(currentStatus == Opened){
            for(int i= 0;i < fighters.size();i++){
                if(fighters.get(i).uuid == player.getUniqueId()){
                    return true;
                }
            }
        }
        return false;
        */
    }


    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
    public void freezePlayerMove (PlayerMoveEvent event) {
        if (CheckFreezed(event.getPlayer())) {
            Location loc = event.getFrom();
            loc.setX(loc.getBlockX() + 0.5);
            loc.setY(loc.getBlockY());
            loc.setZ(loc.getBlockZ() + 0.5);
            event.getPlayer().teleport(loc);
            event.setCancelled(true);
           // event.getPlayer().sendMessage("試合開催するまでうごけません");
        }
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
    public void resetFightTimer(){
        entryTimer = 180;
    }
    public void resetBetTimer(){
        betTimer = 90;
    }

    void showLifeBarToAll(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            lifebar.addPlayer(player);
        }

    }

    void updateLifeBar(){
        Player f0 = Bukkit.getPlayer(fighters.get(0).uuid);
        Player f1 = Bukkit.getPlayer(fighters.get(1).uuid);
        double h0 = f0.getHealth() / f0.getHealthScale();
        double h1 = f1.getHealth() / f1.getHealthScale();

       // serverMessage("scale :"+f0.getHealthScale());
        //serverMessage("h1"+ h1);
        lifebar.setRBar(h0);
        lifebar.setBBar(h1);
    }
    void closeLifeBar(){
        lifebar.clearBar();

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
           // updateLifeBar();
        }

        if (currentStatus == Fighting) {
            fightTimer--;
            if(fightTimer <= 0){
                serverMessage("タイムアウト！！！　");
                cancelGame();
            }

            updateSidebar();
            updateLifeBar();
        }
    }


    /////////////////////////////////
    //      起動
    /////////////////////////////////
    @Override
    public void onEnable() {
        getLogger().info("Enabled");
        this.saveDefaultConfig();
        this.loadConfig();



        getServer().getPluginManager().registerEvents (this,this);

        //
       getCommand("mfc").setExecutor(new FightClubCommand(this));
       getCommand("mfca").setExecutor(new FightClubArenaCommand(this));


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

    void loadConfig(){
        loadArenaConfig();
    }


    /////////////////////////////////
    //      終了
    /////////////////////////////////
    @Override
    public void onDisable() {
        getLogger().info("Disabled");
        lifebar.clearBar();
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
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        log(p.getName()+"ログアウト");
        for(int i=0;i<waiters.size();i++){
            if(waiters.get(i).uuid == p.getUniqueId()){
                serverMessage(p.getName()+"はログアウトしたため、登録リストからはずされた");
                waiters.remove(i);
                updateSidebar();
                break;
            }
        }
        for(int i=0;i<fighters.size();i++) {
            if(fighters.get(i).uuid == p.getUniqueId()){
                serverMessage(p.getName()+"はログアウトしたため、試合をキャンセルします");
                cancelGame();
                break;
            }
        }

        // You don't need a null check here, it will always be a valid player (afaik)
 //       this.plugin.onQuit(p);
    }
    /////////////////////////////////
    //      チャットイベント
    /////////////////////////////////
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        String message = e.getMessage();
      //  GlowAPI.setGlowing(e.getPlayer(), GlowAPI.Color.AQUA, Bukkit.getOnlinePlayers())

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
            command("man10 tpuser "+ fighters.get(0).name + " death");
            command("man10 tpuser "+ fighters.get(1).name + " death");

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





    ////////////////////////////
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
            updateSidebar();
            updateLifeBar();

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
        if( e.getAction() == Action.RIGHT_CLICK_BLOCK  || e.getAction() == Action.LEFT_CLICK_BLOCK ) {
        if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
                Object o = e.getClickedBlock().getState();
                if((o instanceof  Sign) == false){
                    return;
                }

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
                if(s.getLine(1).equalsIgnoreCase("Menu")){
                    gui.createJoinmenu(e.getPlayer());
                    return;
                }
                if(s.getLine(1).equalsIgnoreCase("Admin")){
                    gui.adminMenu(e.getPlayer());
                    return;
                }
                if(s.getLine(1).equalsIgnoreCase("bar")){
                    if(s.getLine(2).equalsIgnoreCase("addplayer")){
                        lifebar.addPlayer(e.getPlayer());
                    }
                    if(s.getLine(2).equalsIgnoreCase("setvisible")){
                            lifebar.setVisible(true);
                    }
                    if(s.getLine(2).equalsIgnoreCase("setrtitle")){
                        lifebar.setRname(s.getLine(3));
                    }
                    if(s.getLine(2).equalsIgnoreCase("setbtitle")){
                        lifebar.setBname(s.getLine(3));
                    }
                    if(s.getLine(2).equalsIgnoreCase("setbbar")){
                        lifebar.setBBar(Double.parseDouble(s.getLine(3)));
                    }
                    if(s.getLine(2).equalsIgnoreCase("setrbar")){
                        lifebar.setRBar(Double.parseDouble(s.getLine(3)));
                    }
                    if(s.getLine(2).equalsIgnoreCase("hide")){
                        lifebar.clearBar();
                    }
                    if(s.getLine(2).equalsIgnoreCase("removebplayer")){
                        lifebar.removeBPlayer(e.getPlayer());
                    }
                    if(s.getLine(2).equalsIgnoreCase("removerplayer")){
                        lifebar.removeRPlayer(e.getPlayer());
                    }
                    if(s.getLine(2).equalsIgnoreCase("reset")){
                        lifebar.resetBar();
                    }
                }
                /*if(s.getLine(1).equalsIgnoreCase("boss")){//ボスバーのサンプル
                    BossBar b = Bukkit.createBossBar("Sho0", BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
                    BossBar r = Bukkit.createBossBar("hashing_bot", BarColor.RED, BarStyle.SOLID, new BarFlag[0]);

                    b.addPlayer(e.getPlayer());
                    r.addPlayer(e.getPlayer());

                    b.setVisible(true);
                    r.setVisible(true);
                }*/


            }


        }
    }

    public void openGUI(Player p){

        if(currentStatus == Opened){
            //
            gui.betMenu(p);
        }else{
            gui.createJoinmenu(p);
        }
        updateSidebar();

    }

    @EventHandler
    public void clickItem(InventoryClickEvent e) {

        if(e.getClickedInventory() == null){
            //例外インベントリの処理
            //実は、インベントリの外枠（インベントリじゃないところ）　でもこのイベントは発動する
            return;
        }
        if(currentStatus == Entry || currentStatus == Opened) {
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


    //////////////////////////////////
    //        アリーナ関係
    //////////////////////////////////
    ArrayList<String> arenas = new ArrayList<String>();
    String            selectedArena = "";
    public int createArena(CommandSender p,String arena){
        if(getArenaIndex(arena) == -1){
            arenas.add(arena);
            getConfig().set("Arenas",arenas);
            saveConfig();;
            p.sendMessage(arena+" is created");
            selectArena(p,arena);
            return -1;
        }
        p.sendMessage(arena+" is already created");
        return arenas.size();
    }
    public int getArenaIndex(String arena){
        for(int i=0;i<arenas.size();i++){
            if(arenas.get(i).equalsIgnoreCase(arena)){
                return i;
            }
        }
        return -1;
    }
    public int deleteArena(CommandSender p,String arena){
        for(int i=0;i<arenas.size();i++){
            if(arenas.get(i).equalsIgnoreCase(arena)){
                arenas.remove(i);
                p.sendMessage(arena+" is deleted");
                getConfig().set("Arenas",arenas);
                saveConfig();
                return i;
            }
        }
        p.sendMessage(arena+" is already deleted");
        return -1;
    }
    public int selectArena(CommandSender p,String arena){
        for(int i=0;i<arenas.size();i++){
            if(arenas.get(i).equalsIgnoreCase(arena)){
                selectedArena = arena;
                getConfig().set("selectedArena",selectedArena);
                saveConfig();
                p.sendMessage(arena+" selected");

                tp((Player)p,selectedArena,"spawn");
                return i;
            }
        }
        p.sendMessage(arena+" not found");
        return -1;
    }
    public int listArena(CommandSender p) {
        p.sendMessage("------arena list-----");
        for (int i = 0; i < arenas.size(); i++) {
            if(arenas.get(i).equalsIgnoreCase(selectedArena)){
                p.sendMessage(arenas.get(i) +":(selected)");

            }else{
                p.sendMessage(arenas.get(i));

            }
        }
        return arenas.size();
    }
    //
    public void tp(Player p,String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
            p.teleport(loc);
            p.sendMessage("§a§lTPしました。");
        }
        return;
    }
    public void tpf(String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
//            p.teleport(loc);
            for(PlayerInformation f :fighters){
                Player p = Bukkit.getPlayer(f.uuid);
                p.teleport(loc);
            }

        }
        return;
    }

    public void tpa(String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
//            p.teleport(loc);
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.teleport(loc);
            }

        }
        return;
    }
    public void settp(Player p,String arena,String name){
        getConfig().set(arena+ ".pos."+name , p.getLocation());
        saveConfig();
        p.sendMessage("§a§lTPロケーションを設定しました。:"+ name);
    }
    void loadArenaConfig(){
        log("Arenaリストをよみこみちう");
        selectedArena = getConfig().getString("selectedArena");
        Object o =  getConfig().get("Arenas");
        if(o != null){
            arenas = (ArrayList<String>)o;
            log("Arenaリストをよんだ");
        }
    }


}
