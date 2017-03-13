package red.man10.fightclub;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import red.man10.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import static red.man10.fightclub.FightClub.Status.*;


public final class FightClub extends JavaPlugin implements Listener {

    String adminPermision = "man10.fightclub.admin";

    FightClubGUI gui = new FightClubGUI(this);
    LifeBar lifebar = new LifeBar(this);
    TitleBar titlebar = new TitleBar(this);

    VaultManager vault = null;

    FightClubData data = null;


    //    Fight ID （データベースキー）OpenFightでアップデートされる
    int fightId = -1;


    int kill0 = 0;
    int kill1 = 0;
    int death0 = 0;
    int death1 = 0;
    double prize0 =0;
    double prize1 =0;
    double kdr0 =0;
    double kdr1 =0;
    String kdrs0 = "";
    String kdrs1 = "";


    String      worldName = "Arena";

    double      entryPrice = 10000;
    double      prize = 0.05;
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
        int         kill;
        int         death;
        double      prize;
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




    public boolean isFighter(UUID uuid){
        for(PlayerInformation pi :fighters){
            if(pi.uuid == uuid){
                return true;
            }
        }
        return false;
    }
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
                return -2;
            }
        }

        //        登録費用
        if(vault.withdraw(uuid,entryPrice) == false){
            return -3;
        }


        //      追加
        PlayerInformation playerInfo = new PlayerInformation();
        playerInfo.uuid = uuid;
        playerInfo.name = name;
        playerInfo.isDead = false;
        playerInfo.returnLoc = Bukkit.getPlayer(uuid).getLocation();
        playerInfo.kill = data.killCount(uuid);
        playerInfo.death = data.deathCount(uuid);
        playerInfo.prize = data.totalPrize(uuid);

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
        //     観戦者登録登録されてたらエラー
        ////////////////////////////////////////
        for(PlayerInformation p : spectators){
            if(p.uuid == uuid){
                //  登録済みエラー表示
                return -2;
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
        for(int i = 0;i < spectators.size();i++){
            PlayerInformation p =  spectators.get(i);
            if(p.uuid == uuid){
                inf = p;
                spectators.remove(i);
                break;
            }
        }

        //      もとの場所にもどす
        Player player = Bukkit.getPlayer(uuid);
        if(player != null){
            player.teleport(inf.returnLoc);
            player.setGameMode(GameMode.SURVIVAL);
        }

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

        for(int i=0;i<fighters.size();i++){

            Player p = Bukkit.getPlayer(fighters.get(i).uuid);
            if(p.isDead() == false){
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
        double total = getTotalBet();
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
    double getTotalBet(){
        double totalBet = 0;
        for(int i = 0;i < bets.size();i++){
            totalBet += bets.get(i).bet;
        }
        return totalBet;
    }


    Boolean canBet(UUID buyerUUID){


        //          ファイターは登録できません
        for(int i=0;i< fighters.size();i++){
            if(fighters.get(i).uuid == buyerUUID){
              // serverMessage( "§d八百長防止のため、選手はベットすることはできません");
                return false;
            }
        }

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
        if( currentStatus != Opened){
            return -2;
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



    //      MFC有効無効
    int enableMFC(CommandSender sender,boolean enable){

        //
        if(!sender.hasPermission(adminPermision)){
            sender.sendMessage("You don't have permission:" + adminPermision);
            return 0;
        }

        if(enable){
            sender.sendMessage("MFCを有効にしています");
            startEntry();
        }else{
            sender.sendMessage("MFCを無効にしています");
            cancelGame();
            this.currentStatus = Closed;
        }
        saveCurrentStatus();
        updateSidebar();
        updateLifeBar();
        return 0;
    }

    //      MFC有効無効
    int reload(CommandSender sender){
        serverMessage("MFC Reloading...");

        loadConfig();


        updateSidebar();
        updateLifeBar();


        serverMessage("MFC Reloaded.");
        return 0;
    }

    //////////////////////////////////////////////
    //      ゲームを中断する  払い戻し後ステータスを Closedへ
    //////////////////////////////////////////////
    int cancelGame(){

        showTitle("試合中断!","試合はキャンセルされ返金されます",3,0);

        //   払い戻し処理
        for (int i = 0;i < bets.size();i++) {
            BetInformation bet = bets.get(i);
           //p.sendMessage("Return money to " + bet.buyerName + " $"+ bet.bet );

            vault.deposit(bet.buyerUUID,bet.bet);
            //Bukkit.getPlayer(bet.buyerName).sendMessage("ゲームがキャンセルされお金を$"+bet.bet+"返金しました。");
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

        unregisterFighter(fighters.get(0).uuid);
        unregisterFighter(fighters.get(1).uuid);

        fighters.clear();


        tpaLobby();
        //     ファイター移動
        //tpf(selectedArena,"spawn");
        startEntry();


        return 0;
    }


    public boolean canStartGame(){

        UUID id0 = fighters.get(0).uuid;
        UUID id1 = fighters.get(1).uuid;

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

        resetFightTimer();




        //      キットを選択

        //      装備を保存
        command("mkit push "+fighters.get(0).name );
        command("mkit push "+fighters.get(1).name );

        command("mkit set "+fighters.get(0).name + " " + selectedKit);
        command("mkit set "+fighters.get(1).name + " " + selectedKit);

        tp(Bukkit.getPlayer(fighters.get(0).uuid),selectedArena,"player1");
        tp(Bukkit.getPlayer(fighters.get(1).uuid),selectedArena,"player2");

        Player f0 = Bukkit.getPlayer(fighters.get(0).uuid);
        Player f1 = Bukkit.getPlayer(fighters.get(1).uuid);

        double o0 = getFighterOdds(f0.getUniqueId());
        double o1 = getFighterOdds(f1.getUniqueId());
        int b0 = getFighterBetCount(f0.getUniqueId());
        int b1 = getFighterBetCount(f1.getUniqueId());


        String inf0 = fighterInfo(fighters.get(0));
        String inf1 = fighterInfo(fighters.get(1));

        String f0o = String.format(" Odds:§b§lx%.2f §f§lScore:§c§l%d ",o0,getScore(fighters.get(0))) ;
        String f1o = String.format(" Odds:§b§lx%.2f §f§lScore:§c§l%d ",o1,getScore(fighters.get(1)));


        this.fightId = data.createFight(selectedArena,selectedKit,f0.getUniqueId(),f1.getUniqueId(),o0,o1,b0,b1,getPrize(),getTotalBet());

        //      init bar
        lifebar.setRname(f0.getName() + f0o + inf0);
        lifebar.setBname(f1.getName() + f1o + inf1);
        lifebar.setVisible(true);
        resetPlayerStatus(f0);
        resetPlayerStatus(f1);


        String subStitle =  f0.getName() + " vs " + f1.getName();

        showTitle("3",subStitle, 0.5,0);
        showTitle("2",subStitle, 0.5,1);
        showTitle("1",subStitle, 0.5,2);

        showTitle("ファイト！！ #"+fightId,subStitle, 1,3);


        f0.hidePlayer(f1);
        f1.hidePlayer(f0);
        f0.showPlayer(f1);
        f1.showPlayer(f0);

        updateSidebar();
        saveCurrentStatus();


        unregisterFighter(f0.getUniqueId());
        unregisterFighter(f1.getUniqueId());
        tpWaiterToArena();
        return 0;
    }
    public void resetPlayerStatus(Player p)
        {


            if (p.hasPotionEffect(PotionEffectType.BLINDNESS) == true)
            {
                p.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            if (p.hasPotionEffect(PotionEffectType.CONFUSION) == true)
            {
                p.removePotionEffect(PotionEffectType.CONFUSION);
            }
            if (p.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE) == true)
            {
                p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            }
            if (p.hasPotionEffect(PotionEffectType.FAST_DIGGING) == true)
            {
                p.removePotionEffect(PotionEffectType.FAST_DIGGING);
            }
            if (p.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) == true)
            {
                p.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            }
            if (p.hasPotionEffect(PotionEffectType.HEAL) == true)
            {
                p.removePotionEffect(PotionEffectType.HEAL);
            }
            if (p.hasPotionEffect(PotionEffectType.HUNGER) == true)
            {
                p.removePotionEffect(PotionEffectType.HUNGER);
            }
            if (p.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE) == true)
            {
                p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            }
            if (p.hasPotionEffect(PotionEffectType.JUMP) == true)
            {
                p.removePotionEffect(PotionEffectType.JUMP);
            }
            if (p.hasPotionEffect(PotionEffectType.POISON) == true)
            {
                p.removePotionEffect(PotionEffectType.POISON);
            }
            if (p.hasPotionEffect(PotionEffectType.REGENERATION) == true)
            {
                p.removePotionEffect(PotionEffectType.REGENERATION);
            }
            if (p.hasPotionEffect(PotionEffectType.SLOW) == true)
            {
                p.removePotionEffect(PotionEffectType.SLOW);
            }
            if(p.hasPotionEffect(PotionEffectType.SLOW_DIGGING) == true)
            {
                p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            }
            if (p.hasPotionEffect(PotionEffectType.SPEED) == true)
            {
                p.removePotionEffect(PotionEffectType.SPEED);
            }
            if (p.hasPotionEffect(PotionEffectType.WATER_BREATHING) == true)
            {
                p.removePotionEffect(PotionEffectType.WATER_BREATHING);
            }
            if (p.hasPotionEffect(PotionEffectType.WEAKNESS) == true)
            {
                p.removePotionEffect(PotionEffectType.WEAKNESS);
            }
            p.setFoodLevel(20);
            p.setExhaustion(0);
            p.setHealth(p.getHealthScale());
    }
    public int startEntry(){
        resetEnetryTimer();
        resetFightTimer();
        resetBetTimer();
        bets.clear();
        fighters.clear();
        currentStatus = Entry;
        closeLifeBar();
        updateSidebar();
        saveCurrentStatus();
        return 0;
    }

    public void saveCurrentStatus(){

        if(currentStatus == Closed){
            getConfig().set("Disabled",true);
        }else{
            getConfig().set("Disabled",false);

        }


        saveConfig();
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
        //      ファイター観戦者ともに移動
        tps(selectedArena,"spawn");
        tpf(selectedArena,"spawn");


        //       KDRの算出
        kill0 = data.killCount(fighters.get(0).uuid);
        kill1 = data.killCount(fighters.get(1).uuid);
        death0 = data.deathCount(fighters.get(0).uuid);
        death1 = data.deathCount(fighters.get(1).uuid);
        prize0 = data.totalPrize(fighters.get(0).uuid);
        prize1 = data.totalPrize(fighters.get(1).uuid);
        kdr0 = 0;
        kdr1 = 0;
        kdrs0 = "";
        kdrs1 = "";
        if(death0 > 0){
            kdr0 = kill0 / death0;
            kdrs0 = String.format("%.2f",kdr0);
        }
        if(death1 > 0){
            kdr1 = kill1 / death1;
            kdrs1 = String.format("%.2f",kdr1);
        }




        clearEntity();

        sideBar.hidden = true;
        String title = "§cMFC 選手決定！!";
        String subTitle = fighters.get(0).name + " vs "+fighters.get(1).name + " Stage:" + selectedArena + " Kit:"+selectedKit;
        titlebar.sendTitleToAllWithSound(title,subTitle,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);

        sideBar.show();
        //String s= f.name + " §9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/§e§l$"+money(f.prize);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                String title = "§4"+fighters.get(0).name ;
                //String subTitle = "Kill :1234 / Death 3444 / KDR:1.5 / 総獲得賞金 $1234567";
                String subTitle = "§9§lKill:"+kill0+" §c§lDeath:"+death0+" §e§l総獲得賞金 $"+(int)prize0;
                titlebar.sendTitleToAllWithSound(title,subTitle,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);


            }
        }, 100);
       getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                String title = "§1"+fighters.get(1).name ;
 //               String subTitle = "Kill :1234 / Death 3444 / KDR:1.5 / 総獲得賞金 $1234567";
//                String subTitle = "Kill:"+kill1+" Death:"+death1+" KDR:"+kdrs1+"総獲得賞金 $"+(int)prize1;
                String subTitle = "§9§lKill:"+kill1+" §c§lDeath:"+death1+" §e§l総獲得賞金 $"+(int)prize1;
                titlebar.sendTitleToAllWithSound(title,subTitle,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);

            }
        }, 200);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                String title = "勝者を予想しベットしてください！ /MFC" ;
                String subTitle = ""+ fighters.get(0).name + " vs " + fighters.get(1).name + " ";
                titlebar.sendTitleToAllWithSound(title,subTitle,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);
            }
        }, 300);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {

                sideBar.hidden = false;
                sideBar.show();
                resetBetTimer();
            }
        }, 500);


        resetBetTimer();


        //      ファイト開始
        currentStatus = Opened;
        updateSidebar();

        saveCurrentStatus();

        return true;
    }


    boolean broadcastTitle = true;
    public void showTitle(String title,String subTitle,double stay,double delay){

        int stayTick = (int)(stay * 20);
        int delayTick = (int)(delay * 20);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                titlebar.sendTitleToAllWithSound(title,subTitle,20,stayTick,20,Sound.ENTITY_WITHER_SPAWN,1,1);
            }
        }, delayTick);


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
        return  getTotalBet() * tax;
    }

    //      賞金
    public double getPrize(){
        if(fighters.size() < 2) {
            return 0;
        }
        double t = getTotalBet() * prize;
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
    public int endGame(int fighterIndex){

        if (fighterIndex == -1){
            return cancelGame();
        }



        PlayerInformation pf = fighters.get(fighterIndex);
        Player winner = Bukkit.getPlayer(pf.uuid);

        int loserIndex = -1;
        if (fighterIndex == 0){
            loserIndex = 1;
        }else{
            loserIndex = 0;
        }
        PlayerInformation lf = fighters.get(loserIndex);
        //      勝負結果を保存
        double dr = 120 - fightTimer;
        data.updateFight(fightId,fighterIndex,pf.uuid,lf.uuid,dr);


        double prize = getPrize();
        serverMessage("[MFC]==============結果発表============");

        serverMessage("勝者："+winner.getDisplayName() +"は優勝賞金 $"+(int)prize+"をゲットした！！！！");
        vault.deposit(winner.getUniqueId(),prize);

        //  掛け金の計算
        double total  = getTotalBet();
        double winBet = getFighterBets(fighterIndex);

        showTitle("勝者: "+winner.getName(),"獲得賞金:$"+(int)prize,5,0);
        //    オッズとは
        //  （賭けられたお金の合計 － 経費）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - getCost()) / winBet;

        for (int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            PlayerInformation f = fighters.get(bet.fighterIndex);
            if (bet.fighterIndex != fighterIndex){
                data.createBet(fightId,bet.buyerUUID,bet.bet,false,f.uuid,odds,bet.bet * -1);
                continue;
            }
            //      プレイヤーへの支払い金額
            double playerPayout = bet.bet * odds;

            //      プレイヤーへ支払い
            serverMessage("[MFC]"+bet.buyerName+"は、予想があたり、$"+(int)playerPayout+"をゲットした！！ Odds:x"+String.format("%.2f",odds));

            //      通知
            vault.deposit(bet.buyerUUID,playerPayout);

            //      データベース登録

            double profit = playerPayout - bet.bet;
            data.createBet(fightId,bet.buyerUUID,bet.bet,true,f.uuid,odds,profit);
        }

        //
        unregisterFighter(fighters.get(0).uuid);
        unregisterFighter(fighters.get(1).uuid);


        tpaLobby();

        //      終了

        startEntry();
        updateSidebar();

        return 0;
    }

    int      entryTimer = 0;
    int      betTimer = 0;
    int      fightTimer = 0;

    public void resetEnetryTimer(){
        entryTimer = 30;
    }
    public void resetFightTimer(){
        fightTimer = 120;
    }
    public void resetBetTimer(){
        betTimer = 30;
    }

    void showLifeBarToAll(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            lifebar.addPlayer(player);
        }

    }

    void updateLifeBar(){

        if(fighters.size() >= 2){
            Player f0 = Bukkit.getPlayer(fighters.get(0).uuid);
            Player f1 = Bukkit.getPlayer(fighters.get(1).uuid);
            double h0 = f0.getHealth() / f0.getHealthScale();
            double h1 = f1.getHealth() / f1.getHealthScale();

            lifebar.setRBar(h0);
            lifebar.setBBar(h1);
        }
    }
    void closeLifeBar(){
        lifebar.clearBar();

    }
    public void onTickTimer(){
        //updateSigns();

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


     String fighterInfo(FightClub.PlayerInformation f){
        String s = "§9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/§e§l$"+money(f.prize);
        return s;
    }
    //          描画系
    private static String[] suffix = new String[]{"","K", "M", "B", "T"};
    private static int MAX_LENGTH = 4;

    private static String money(double number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while(r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")){
            r = r.substring(0, r.length()-2) + r.substring(r.length() - 1);
        }
        return r;
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

        //data = new FightClubData(this);


        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                onTimer();
            }
        }, 0, 20);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                onTickTimer();
            }
        }, 0, 1);
    }

    void loadConfig(){

        loadArenaConfig();
        loadSignes();


        //      MYSQL初期化
        data = new FightClubData(this);

        boolean flag = getConfig().getBoolean("Disabled");
        if(flag == true){
            currentStatus = Closed;
        }

        updateSidebar();

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
        updateSidebar();
        tpLobby(p);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        log(p.getName() + "ログアウト");

        if(p.isOnline()){
            p.setGameMode(GameMode.SURVIVAL);
            tpLobby(p);
        }


        if(unregisterFighter(p.getUniqueId()) < 0){
            serverMessage(p.getName() + "はログアウトしたため、登録リストからはずされた");
            updateSidebar();
        }

        for (int i = 0; i < fighters.size(); i++) {
            if (fighters.get(i).uuid == p.getUniqueId()) {
                serverMessage(p.getName() + "はログアウトしたため、試合をキャンセルします");
                cancelGame();
                break;
            }
        }

        if (unregisterSpectator(p.getUniqueId()) != -1){
            serverMessage(p.getName()+"はログアウトしたため、観戦をやめました");
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
    public void onPlayerDeath(PlayerDeathEvent e)
    {

        if(currentStatus != Fighting){
            return;
        }

        Player p = (Player)e.getEntity();

        int index = getFighterIndex(p.getUniqueId());
        //

        if(index != -1){
            PlayerInformation f = fighters.get(index);      //  死亡者

            fighters.get(index).isDead = true;
            serverMessage("死亡!!!:"+p.getDisplayName());
        //    p.sendMessage("あなたは、観戦者になりました。");



            //command("man10 tpuser "+ fighters.get(0).name + " death");
            //command("man10 tpuser "+ fighters.get(1).name + " death");

            //      最後ならゲームを終了する
            if(getAliveFighterCount() <= 1){
                serverMessage("ゲーム終了！！！");



                command("mkit pop "+fighters.get(0).name );
                command("mkit pop "+fighters.get(1).name );
                tpaLobby();



                int lastIndex = getLastFighter();
                endGame(lastIndex);




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




    @EventHandler
    public void PlayerDamageReceive(EntityDamageByEntityEvent e) {
        if(currentStatus == Closed){
            return ;
        }


        if(e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();

            if((e.getDamager() instanceof  Player) == false){

                return ;
            }

            Player damager = (Player) e.getDamager();

            //
            if(!isFighter(damager.getUniqueId())){
                damager.sendMessage("選手以外の戦闘行動は禁止されています");
                e.setCancelled(true);
                return;
            }

            //  死亡をキャンセル
            if((damaged.getHealth()-e.getDamage()) <= 0) {
                e.setCancelled(true);

                serverMessage("[MFC]ゲーム終了:"+damaged.getDisplayName()+"は死亡した");

                resetPlayerStatus(damaged);


                int index = getFighterIndex(damager.getUniqueId());
                endGame(index);

                for(PlayerInformation pf : fighters){
                    command("mkit pop "+pf.name);
                }

                tpaLobby();


            }


        }
    }

    int  getScore(FightClub.PlayerInformation inf){
        double d = inf.prize /  (double)(inf.kill + inf.death) * 0.001;
        return (int)d;
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

        if(currentStatus == Closed){
            return;
        }

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
            if (s.getLine(1).equalsIgnoreCase("UnRegister")) {
                unregisterFighter(e.getPlayer().getUniqueId());
                return;
            }

                if (s.getLine(1).equalsIgnoreCase("Entry")) {
                    startEntry();
                    return;
                }
            if (s.getLine(1).equalsIgnoreCase("Lobby")) {
                tpLobby(e.getPlayer());
                return;
            }

            if (s.getLine(1).equalsIgnoreCase("Watch")) {
                tp(e.getPlayer(),selectedArena,"spawn");
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
                    //gui.adminMenu(e.getPlayer());
                    return;
                }
                if(s.getLine(1).equalsIgnoreCase("Kit")){
                    log("Kit");
                    registerKitSign(e.getPlayer(),e.getClickedBlock().getLocation());
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
        if(currentStatus == Closed){
            return ;
        }

        if(currentStatus == Opened){
            gui.betMenu(p);
        }else{
            gui.createJoinmenu(p);
        }
        updateSidebar();

    }

    @EventHandler
    public void clickItem(InventoryClickEvent e) {
        /*if(currentStatus == Closed){
            return;
        }*/
        if(!e.getInventory().getName().equals(e.getInventory().getName())){
            return;
        }
        if(e.getClickedInventory() == null){
            //例外インベントリの処理
            //実は、インベントリの外枠（インベントリじゃないところ）　でもこのイベントは発動する
            return;
        }
        //if(currentStatus == Entry || currentStatus == Opened) {
            gui.clickItem(e);
       //}

    }

    //      auto update signs
    ArrayList<Location> kitSigns = new ArrayList<Location>();
    boolean registerKitSign(Player p,Location loc){
        for(Location s: kitSigns){
            if(s.getX() == loc.getX() && s.getY() == loc.getY() && s.getZ() == loc.getZ()){
                p.sendMessage("この座標は登録されいてる");
                return false;
            }

        }
        //      座標を保存
        kitSigns.add(loc);
        getConfig().set("KitSigns",kitSigns);
        saveConfig();;
        p.sendMessage("登録しました");
        return true;
    }
    void loadSignes(){
        log("signリストをよみこみちう");

        Object o =  getConfig().get("KitSigns");
        if(o != null){
            kitSigns = (ArrayList<Location>)o;
            log("KitSignsリストをよんだ");
        }

    }
    int  tickCounter = 0;
    void updateSigns(){
        for(Location s: kitSigns){
            Sign sign = (Sign)s.getBlock().getState();
            if(sign instanceof  Sign){
                if(sign.getLine(0).equalsIgnoreCase("[MFC]")){
                    if(sign.getLine(1).equalsIgnoreCase("kit")){
                        sign.setLine(2,""+tickCounter);
                        sign.update();

                        tickCounter++;
                    }
                    if(sign.getLine(1).equalsIgnoreCase(worldName)){
                        sign.setLine(2,selectedArena);
                    }
                }
            }
        }
    }
    @EventHandler
    public void creativeInventory(InventoryCreativeEvent e){
        return;
    }

    //      ブロックがこわれた
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        signCheck(e.getBlock());
    }

    public boolean signCheck(Block b) {
        if(currentStatus == Closed){
            return false;
        }


        for (BlockFace f : BlockFace.values()) {
            if (b.getRelative(f).getType() == Material.WALL_SIGN) {
                for(int i = 0;i < kitSigns.size();i++) {
                    Location loc = kitSigns.get(i);
                    if(loc.getX() == b.getX() && loc.getY() == b.getY() && loc.getZ() == b.getZ()){
                        serverMessage("看板を削除しました");
                        kitSigns.remove(i);
                        return true;
                    }
                }
            }
        }
        return false;
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


            updateEntities(p,getPlayersWithin(p,100),true);

        }
        return;
    }
    public void tpf(String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
//            p.teleport(loc);
            for(PlayerInformation f :waiters){
                Player p = Bukkit.getPlayer(f.uuid);
                p.teleport(loc);
                updateEntities(p,getPlayersWithin(p,100),true);
            }

        }
        return;
    }
    public void tpw(String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
//            p.teleport(loc);
            for(PlayerInformation f :waiters){
                Player p = Bukkit.getPlayer(f.uuid);
                p.teleport(loc);
            }

        }
        return;
    }

    public void tps(String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
//            p.teleport(loc);
            for(PlayerInformation f :spectators){
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
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.teleport(loc);

                updateEntities(p,getPlayersWithin(p,100),true);

            }
         //   Bukkit.getWorld(worldName).refreshChunk();
        }
        return;
    }
    public void settp(Player p,String arena,String name){
        getConfig().set(arena+ ".pos."+name , p.getLocation());
        saveConfig();
        p.sendMessage("§a§lTPロケーションを設定しました。:"+ name);
    }

    public void setlobby(Player p){
        getConfig().set("lobby" , p.getLocation());
        saveConfig();
        p.sendMessage("§a§lTPロケーションを設定しました。:");
    }
    public void tpWaiterToArena(){


        int n = 0;
        for(PlayerInformation inf : waiters){
            Player p = Bukkit.getPlayer(inf.uuid);

            if(p.isOnline() == false){
                continue;
            }
            p.setGameMode(GameMode.SPECTATOR);

            if((n % 2) == 0){
                tp(p,selectedArena,"player1");
            }else{
                tp(p,selectedArena,"player1");
            }
            n++;
        }

    }
    public void tpaLobby(){
        Object o =  getConfig().get("lobby");
        if(o != null){
            Location loc = (Location)o;
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                if(player.getLocation().getWorld().getName().equalsIgnoreCase(worldName)){
                    player.teleport(loc);
                    player.setGameMode(GameMode.SURVIVAL);

                    //      ブルブルバグ
                    updateEntities(player,getPlayersWithin(player,100) ,true);
                }
            }


        }
        return;
    }
    public void tpLobby(Player p){
        Object o =  getConfig().get("lobby");
        if(o != null){
            Location loc = (Location)o;
            p.teleport(loc);
        }
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

    @EventHandler
    public void itemSpawn(ItemSpawnEvent e) {
        if(currentStatus == Closed){
            return;
        }

        if(e.getLocation().getWorld().getName().equalsIgnoreCase(worldName)){
            e.getEntity().remove();

        }
    }
    //      エンティティを消す
    void clearEntity(){
        for(Entity en : Bukkit.getServer().getWorld(worldName).getEntitiesByClass(Item.class)){
            en.remove();;
        }
    }

    //      Invisible Bug Fix

    private List<Player> getPlayersWithin(Player player, int distance) {
        List<Player> res = new ArrayList<Player>();
        int d2 = distance * distance;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != player && p.getWorld() == player.getWorld()
                    && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
                res.add(p);
            }
        }
        return res;
    }

    private void updateEntities(Player tpedPlayer, List<Player> players, boolean visible) {
        // Hide or show every player to tpedPlayer
        // and hide or show tpedPlayer to every player.
        for (Player player : players) {
            if (visible) {
                tpedPlayer.showPlayer(player);
                player.showPlayer(tpedPlayer);
            } else {
                tpedPlayer.hidePlayer(player);
                player.hidePlayer(tpedPlayer);
            }
        }
    }


}
