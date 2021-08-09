package red.man10.fightclub;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import red.man10.LifeBar;
import red.man10.TitleBar;
import red.man10.Utility;
import red.man10.VaultManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static red.man10.fightclub.FightClub.Status.*;


public final class FightClub extends JavaPlugin implements Listener {

    //      初期設定
    long        entryPrice = 10000;        //  ミニマムベットプライス
    double      prize = 0.05;
    double      tax   = 0;

    int         betLimit = 1000;

    int         newbiePlayableCount = 10;
    double      registerKDRLimit = 0.2;

    String adminPermision = "man10.fightclub.admin";
    String betPermision = "man10.fightclub.bet";
    String registerPermision = "man10.fightclub.register";
    String unregisterPermision = "man10.fightclub.unregister";

    FightClubGUI gui = new FightClubGUI(this);
    LifeBar lifebar = new LifeBar(this);
    TitleBar titlebar = new TitleBar(this);

    VaultManager vault = null;
    FightClubData data = null;


    //    Fight ID （データベースキー）OpenFightでアップデートされる
    int fightId = -1;

    String      worldName = "mfc";

    public enum MFCModes {
        Off,
        Free,           //  自由に遊べる、通知はしない
        Normal,         //  通常のモード（グローバル通知はしない
        WhiteList,      //  ホワイトリスト
        Pro             //  グローバル通知あり
    }
    MFCModes           mode;

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
        //
        int         kill;
        int         death;
        double      prize;
        void updateKDRP(FightClubData data){
            kill = data.killCount(uuid);
            death = data.deathCount(uuid);
            prize = data.totalPrize(uuid);
        }
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
    ArrayList<PlayerInformation> waiters = new ArrayList<>();

    //      対戦者リスト
    ArrayList<PlayerInformation> fighters = new ArrayList<>();

    //      観戦者リスト
   // ArrayList<PlayerInformation> spectators = new ArrayList<PlayerInformation>();

    //      掛け金
    ArrayList<BetInformation> bets = new ArrayList<>();

    /*
    //      ブラックリスト
    ArrayList<UUID> blacklist = new ArrayList<UUID>();
    //      ホワイトリスト
    ArrayList<UUID> whitelist = new ArrayList<UUID>();
    //     Pro
    ArrayList<UUID> prolist = new ArrayList<UUID>();
    Boolean enbaleBlackList = true;
    Boolean enableWhitelist = true;
    */

    FightClubList whitelist = null;
    FightClubList blacklist = null;
    FightClubList prolist = null;

    public  void setMFCMode(CommandSender sender,MFCModes mode){

        cancelGame();
        this.mode = mode;

        String title = "";
        String subTitle = "";
        if(mode == MFCModes.Off) {
            title = "MFC 終了します";
            enableMFC(sender, false);
        }
        if(mode == MFCModes.Free){
            title = "§dMFC フリーモード";
            subTitle = "誰でも参加練習できます。参加費無料！記録なし";
            enableMFC(sender,true);

        }
        if(mode == MFCModes.Normal){
            title = "§bMFC 通常モード";
            subTitle = "誰でも参加できます。KDR0.2以下になると参加できません";
            enableMFC(sender,true);

        }
        if(mode == MFCModes.WhiteList){
            title = "MFC ホワイトリスト";
            subTitle = "参加資格がある者のみ参加できます";
            enableMFC(sender,true);
        }
        if(mode == MFCModes.Pro){
            title = "§e§kXXXXX  §4§l【MFC Pro】§e§kXXXXX";
            subTitle = "§e§l勝者を予想して、§e§n大金§e§lをゲットしよう！！！";

            enableMFC(sender,true);
        }


        sendTitleToAllWithSound(title,subTitle,20,140,20,Sound.ENTITY_WITHER_SPAWN,1,1);

    }

    //      タイトルバーに表示
    public void sendTitleToAllWithSound(String mainText,String subText,int fadeIn,int stay,int fadeOut,Sound s,float volume,float pitch) {
        if( mode == MFCModes.Pro){
            titlebar.sendTitleToAllWithSound(mainText,subText,fadeIn,stay,fadeOut,s,volume,pitch);
            return;
        }
        for(Player p :Bukkit.getWorld(worldName).getPlayers()){
            titlebar.sendTitleWithSound(p,mainText,subText,fadeIn,stay,fadeOut,s,volume,pitch);
        }

    }


        public boolean isFighter(UUID uuid){
        for(PlayerInformation pi :fighters){
            if(pi.uuid == uuid){
                return true;
            }
        }
       // Bukkit.getLogger().info("");
        return false;
    }
    public boolean unregisterFighter(UUID uuid){
        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(int i = 0;i < waiters.size();i++){
            PlayerInformation fighter = waiters.get(i);
            if(fighter.uuid == uuid){

                waiters.remove(i);
                updateSidebar();;

//                vault.deposit(uuid, entryPrice);
                return true;
            }
        }
        return false;
    }
    ////////////////////////////////
    //       対戦者登録
    ////////////////////////////////
    public int registerFighter(CommandSender s,UUID uuid,String name){

        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(PlayerInformation waiter : this.waiters){
            if(waiter.uuid == uuid){
                //  登録済みエラー表示
                s.sendMessage("すでに登録ずみです");
                return -1;
            }
        }
        Player p = Bukkit.getPlayer(uuid);
        if(mode != MFCModes.Free){
            if(vault.getBalance(uuid) < entryPrice){
                s.sendMessage("参加費用が足りません");
                return -3;
            }
        }

        if(blacklist.find(uuid.toString()) != -1){
            s.sendMessage("ブラックリストに登録されているため参加できません");
            return -5;
        }


        //      プロモード中はプロしか登録できない
        if( mode == MFCModes.Pro){
            if(prolist.find(uuid.toString()) == -1){
                s.sendMessage("プロしか参加できません");
                return -6;
            }
        }

        //      プロモード中はプロしか登録できない
        if( mode == MFCModes.Normal){
            if(prolist.find(uuid.toString()) != -1){
                s.sendMessage("プロは通常モードに参加できません");
                return -7;
            }
        }


        //      プロモード中はプロしか登録できない
        if( mode == MFCModes.WhiteList){
            if(whitelist.find(uuid.toString()) == -1){
                s.sendMessage("あなたはホワイトリストに追加されていません");
                return -7;
            }
        }


        //      追加
        PlayerInformation playerInfo = new PlayerInformation();
        playerInfo.uuid = uuid;
        playerInfo.name = name;
        playerInfo.isDead = false;
        playerInfo.returnLoc = Bukkit.getPlayer(uuid).getLocation();
        if(data != null){
            playerInfo.kill = data.killCount(uuid);
            playerInfo.death = data.deathCount(uuid);
            playerInfo.prize = data.totalPrize(uuid);
        }

        if(mode != MFCModes.Free){
            resetEnetryTimer();
        }

        //String str = String.format("");

        int play = playerInfo.kill + playerInfo.death;
        String kdrs = "0.00";
        if(playerInfo.death != 0){
            double kdr = (double)playerInfo.kill / (double)playerInfo.death;
            kdrs = String.format("%.2f",kdr);
        }
        if(mode != MFCModes.Free) {
            //        登録費用
            if (!vault.withdraw(uuid, entryPrice)) {
                s.sendMessage("参加費用がありません");
                return -3;
            }

            /////////////////////////////////////
            //       参加資格チェック
            /////////////////////////////////////
            if(play >= newbiePlayableCount && playerInfo.death != 0){
                double kdr = (double)playerInfo.kill / (double)playerInfo.death;
                if(kdr < registerKDRLimit){
                    if(whitelist.find(p.getUniqueId().toString()) != -1){

                        serverMessage(playerInfo.name +"は、弱すぎて参加資格がないが、今回は特別に許された。");
                        waiters.add(playerInfo);
                        String his = name + " Kill:"+  playerInfo.kill + " Death:"+playerInfo.death + " 総プレイ数:"+play + " KDR:"+kdrs;
                        serverMessage(his);

                        return waiters.size();
                    }


                    //s.sendMessage("ブラックリストに登録されているため参加できません");
                    s.sendMessage(playerInfo.name +"は、MFCに登録しようとしましたが、弱すぎるため拒否されました。KDR:"+registerKDRLimit+"以上が最低条件です");
                    return -4;
                }
            }
        }





        String his = name + " Kill:"+  playerInfo.kill + " Death:"+playerInfo.death + " "+ Utility.getPriceString(playerInfo.prize) + " 総プレイ数:"+play + " KDR:"+kdrs;
        serverMessage(name + "は参加を申し込んだ");
        serverMessage(his);



        waiters.add(playerInfo);

        updateSidebar();
        return waiters.size();
    }

    public int addBlocklist(CommandSender sender,UUID uuid){
        return 0;
    }
    public int whiteBlocklist(CommandSender sender,UUID uuid){
        return 0;

    }

    public int kick(CommandSender sender,UUID uuid){

        return 0;
    }
    public int ban(CommandSender sender,UUID uuid){

        return 0;
    }

    //      観戦者
    public int registerSpectator(UUID uuid){

        //      スポンへ移動
        Player p = Bukkit.getPlayer(uuid);
        tp(p,selectedArena,"spawn");
      //  p.setGameMode(GameMode.SPECTATOR);

   //     updateSidebar();
     //   return spectators.size();
        return 0;
    }

    public int unregisterSpectator(UUID uuid){

        //      もとの場所にもどす
        Player player = Bukkit.getPlayer(uuid);
        player.setGameMode(GameMode.SURVIVAL);
        tpLobby(player);

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
        for (PlayerInformation fighter : fighters) {
            if (!fighter.isDead) {
                ret++;
            }
        }
        return ret;
    }
    //      生存者数
    int getLastFighter() {
        int ret = 0;

        for(int i=0;i<fighters.size();i++){

            Player p = Bukkit.getPlayer(fighters.get(i).uuid);
            if(!p.isDead()){
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
        for (BetInformation bet : bets) {
            if (bet.fighterIndex == index) {
                count++;
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
        return (total - getCost()) / bet;
    }

    double getFighterBets(int fighterIndex){
        double totalBet = 0;
        for (BetInformation bet : bets) {
            if (bet.fighterIndex == fighterIndex) {
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
        for (BetInformation bet : bets) {
            totalBet += bet.bet;
        }
        return totalBet;
    }


    Boolean canBet(UUID buyerUUID){


        //          ファイターは登録できません
        for (PlayerInformation fighter : fighters) {
            if (fighter.uuid == buyerUUID) {
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

        if(price <= 0){
            return -1;
        }


        int index = getFighterIndex(fighterUUID);
        if(index == -1){
            return -1;
        }

        if(!canBet(buyerUUID)){
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
                double odds = getFighterOdds(fighterUUID);
                String ods = String.format("§b§l倍率:%.2f倍",odds);



                serverMessage(buyerName+"は"+fighters.get(index).name+"へ"+Utility.getPriceString(price)+ "§f追加ベットした! -> "+ods);
                return i;
            }
        }

        BetInformation bet = new BetInformation();
        bet.bet = price;
        bet.fighterIndex = index;
        bet.buyerUUID = buyerUUID;
        bet.buyerName = buyerName;
        bets.add(bet);
        double odds = getFighterOdds(fighterUUID);
        String ods = String.format("§b§l倍率:%.2f倍",odds);

        String mes =  buyerName+"は"+fighters.get(index).name+"へ§e"+Utility.getPriceString(price)+"§fベットした！ -> "+ods;
        serverMessage(mes);

      //  lifebar.setInfoName(mes);

        if(mode == MFCModes.Pro){
            if(price >= 10000){
                resetBetTimer();
            }

        }else{
            resetBetTimer();

        }



        return bets.size();
    }

    boolean checkAdminPermission(CommandSender sender){
        if(!sender.hasPermission(adminPermision)){
            sender.sendMessage("You don't have permission:" + adminPermision);
            return true;
        }
        return false;
    }

    //      MFC有効無効
    int enableMFC(CommandSender sender,boolean enable){

        //      管理者権限チェック
        if(checkAdminPermission(sender)){
            return 0;
        }

        if(enable){
            sender.sendMessage("MFCを有効にしています");
            startEntry();
        }else{
            sender.sendMessage("MFCを無効にしています");
            cancelGame();
            closeLifeBar();
            closeInfoBar();
            this.currentStatus = Closed;
        }

        saveCurrentStatus();
        updateSidebar();
        updateLifeBar();
        return 0;
    }

    //      MFC有効無効
    int reload(CommandSender sender){
        //      管理者権限チェック
        if(checkAdminPermission(sender)){
            return 0;
        }

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
        serverMessage("試合中断！ 試合はキャンセルされ返金されます");

        //   払い戻し処理
        for (BetInformation bet : bets) {
            vault.deposit(bet.buyerUUID, bet.bet);
        }
        bets.clear();

        resetBetTimer();
        resetEnetryTimer();
        resetFightTimer();

        for(PlayerInformation p : fighters){
            unregisterFighter(p.uuid);
            command("man10kit pop "+p.name);
            //sserverMessage(p.name +"のインベントリをもどしています");
        }

        fighters.clear();


        tpaLobby();
        //     ファイター移動
        //tpf(selectedArena,"spawn");
        startEntry();


        return 0;
    }

    boolean pauseTimer = false;



    public boolean canStartGame(){

        if(mode == MFCModes.Free){
            return true;
        }


        UUID id0 = fighters.get(0).uuid;
        UUID id1 = fighters.get(1).uuid;

        double    limit = betLimit;
        //      双方にベットされているか
        if(getFighterBetMoney(fighters.get(0).uuid) < limit){
            return false;
        }
        //      双方にベットされているか
        return !(getFighterBetMoney(fighters.get(1).uuid) < limit);
    }
    //      募集開始

    public int startGame(){
        gui.closeInMenu();
        if(fighters.size() < 2){
            serverMessage("二人以上いないと開催できませんキャンセルします");
            cancelGame();
            return 0;
        }


        if(!canStartGame()){
            serverMessage("ベットされた金額が足らないため試合をキャンセルします");
            cancelGame();
            return 0;
        }

        sideBar.show();
        pauseTimer = true;
        updateInfoBar();

        showLifeBar();

        resetFightTimer();

        Player f0 = Bukkit.getPlayer(fighters.get(0).uuid);
        Player f1 = Bukkit.getPlayer(fighters.get(1).uuid);

        assert f0 != null;
        assert f1 != null;

        tp(f0,selectedArena,"player1");
        tp(f1,selectedArena,"player2");

        double o0 = getFighterOdds(f0.getUniqueId());
        double o1 = getFighterOdds(f1.getUniqueId());
        int b0 = getFighterBetCount(f0.getUniqueId());
        int b1 = getFighterBetCount(f1.getUniqueId());


        String inf0 = fighterInfo(fighters.get(0));
        String inf1 = fighterInfo(fighters.get(1));

//        String f0o = String.format(" Odds:§b§lx%.2f §f§lScore:§c§l%d ",o0,getScore(fighters.get(0))) ;
//        String f1o = String.format(" Odds:§b§lx%.2f §f§lScore:§c§l%d ",o1,getScore(fighters.get(1)));
        String f0o = String.format(" 倍率:§b§l%.2f倍 §f§lScore:§c§l%d ",o0,getScore(fighters.get(0))) ;
        String f1o = String.format(" 倍率:§b§l%.2f倍 §f§lScore:§c§l%d ",o1,getScore(fighters.get(1)));


        if(mode != MFCModes.Free){
            this.fightId = data.createFight(selectedArena,selectedKit,f0.getUniqueId(),f1.getUniqueId(),o0,o1,b0,b1,getPrize(),getTotalBet());
        }

        //      init bar
        lifebar.setRname(f0.getName() + f0o + inf0);
        lifebar.setBname(f1.getName() + f1o + inf1);
        lifebar.setVisible(true);
        resetPlayerStatus(f0);
        resetPlayerStatus(f1);

        command("man10kit set " + f0.getName() + " " + selectedKit);
        command("man10kit set " + f1.getName() + " " + selectedKit);


        String subTitle =  "§1"+f0.getName() + " §fvs " + "§4"+f1.getName();

        showTitle("§c3",subTitle, 0.5,0);
        showTitle("§c2",subTitle, 0.5,1);
        showTitle("§c1",subTitle, 0.5,2);

        String title = "§cファイト！！ #"+fightId;
        showTitle(title,subTitle, 1,3);
        serverMessage(title);
        serverMessage(subTitle);

        // for spigot invisible bug
        f0.hidePlayer(this,f1);
        f1.hidePlayer(this,f0);
        f0.showPlayer(this,f1);
        f1.showPlayer(this,f0);


        updateSidebar();
        saveCurrentStatus();


        unregisterFighter(f0.getUniqueId());
        unregisterFighter(f1.getUniqueId());
        tpWaiterToArena();

        currentStatus = Fighting;
        sideBar.show();
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
               pauseTimer = false;


            }
        }, 20*3);

        return 0;
    }
    public void resetPlayerStatus(Player p)
        {
            p.setFireTicks(0);
            p.getInventory().clear();

            for (PotionEffect type : p.getActivePotionEffects()){
                p.removePotionEffect(type.getType());
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
        showInfoBarToPlayer();
        updateSidebar();
        saveCurrentStatus();
        pauseTimer = false;
        lifebar.setInfoBar(0);
        return 0;
    }

    public void saveCurrentStatus(){

        if(currentStatus == Closed){
            getConfig().set("Disabled",true);
        }else{
            getConfig().set("Disabled",false);

        }

       // getConfig().set("CurrentMode",mode);


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
        for (PlayerInformation f : waiters) {
            Player p = Bukkit.getPlayer(f.uuid);
            if (p != null) {
                fighters.add(f);
                if (fighters.size() >= max) {
                    break;
                }
            }
        }

        if (fighters.size() < 2){
            serverMessage("選手が足らないためMFC開始できません");
            fighters.clear();
            return false;
        }
        //  Kill/Death/Prize更新
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            for(PlayerInformation inf : fighters){
                inf.updateKDRP(data);
            }
        });

        for(PlayerInformation fi : fighters) {
            //      装備を保存
             command("man10kit push "+fi.name );
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
        tpf(selectedArena,"spawn");


        PlayerInformation f0 = fighters.get(0);
        PlayerInformation f1 = fighters.get(1);


        lifebar.setInfoBar(0);

        clearEntity();

        //      フリーモードなら
        if(mode == MFCModes.Free){
            startGame();
            return true;
        }

        sideBar.hidden = true;
        String title = "§cMFC 選手決定！!";
        String subTitle = "§4§l"+f0.name + " §fvs §1§l"+f1.name + " §aステージ:" + selectedArena + " §bキット:"+selectedKit;
        sendTitleToAllWithSound(title,subTitle,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);

        sideBar.show();
        //String s= f.name + " §9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/§e§l$"+money(f.prize);

        serverMessage("§e============== §d●§f●§a●§eMan10 Fight Club 選手決定§d●§f●§a● §e===============");
        serverMessage(subTitle);

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            String title1 = "§4§l"+f0.name ;
            //String subTitle = "Kill :1234 / Death 3444 / KDR:1.5 / 総獲得賞金 $1234567";
            String subTitle1 = "§9§lKill:"+f0.kill+" §c§lDeath:"+f0.death+" §e§l総獲得賞金 "+ Utility.getPriceString(f0.prize);
            sendTitleToAllWithSound(title1, subTitle1,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);
            serverMessage(title1 + "§f: "+ subTitle1);
           // serverMessage("§f§lユーザー情報はここをクリック！ => §b§l§nhttp://man10.red/u?"+f0.name);
        }, 100);
       getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
           String title12 = "§1§l"+f1.name;
           String subTitle12 = "§9§lKill:"+f1.kill+" §c§lDeath:"+f1.death+" §e§l総獲得賞金 "+Utility.getPriceString(f1.prize);
           sendTitleToAllWithSound(title12, subTitle12,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);
           serverMessage(title12 + "§f: "+ subTitle12);
          // serverMessage("§f§lユーザー情報はここをクリック! => §b§l§nhttp://man10.red/u?"+f1.name);

       }, 200);

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            String title13 = "勝者を予想してください！ §a§l/MFC" ;
            String subTitle13 = "§4§l"+ f0.name + " §fvs §1§l" +f1.name + " ";
            sendTitleToAllWithSound(title13, subTitle13,40,100,40,Sound.ENTITY_WITHER_SPAWN,1,1);
            serverMessage(subTitle13);
            serverMessage(title13);
        }, 300);

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {

            sideBar.hidden = false;
            sideBar.show();
            resetBetTimer();
         //  pauseTimer = false;
        }, 500);


        resetBetTimer();


        //      ファイト開始
        currentStatus = Opened;
        updateSidebar();

        saveCurrentStatus();


        if(autoBetPrice >= 1000){
            UUID uuid = UUID.fromString(autoBetUUID);
            betFighter(fighters.get(0).uuid,autoBetPrice,uuid,autoBetPlayerName);
            betFighter(fighters.get(1).uuid,autoBetPrice,uuid,autoBetPlayerName);
        }

        return true;
    }

    double autoBetPrice = 0;
    String autoBetPlayerName = "MFC Auto Bet";
    String autoBetUUID = "43986c19-28ec-408f-a217-5a97b4ac8991";

    boolean broadcastTitle = true;
    public void showTitle(String title,String subTitle,double stay,double delay){

        int stayTick = (int)(stay * 20);
        int delayTick = (int)(delay * 20);

        MFCModes m = this.mode;
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if(m == MFCModes.Pro){
                titlebar.sendTitleToAllWithSound(title,subTitle,20,stayTick,20,Sound.ENTITY_WITHER_SPAWN,1,1);
            }else{

                for(Player p :Bukkit.getOnlinePlayers()){
                    if(p != null){
                        if(p.getWorld().getName().toString().equalsIgnoreCase(selectedArena)){
                            titlebar.sendTitleWithSound(p,title,subTitle,20,20,20,Sound.ENTITY_WITHER_SPAWN,1,1);

                        }
                    }
                }
            }
        }, delayTick);


    }


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

        //      本人登録
        if(cmd.getName().equalsIgnoreCase("mfcr")){
            Player p = (Player)sender;
            registerFighter(sender,p.getUniqueId(),p.getName());
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

        if(mode == MFCModes.Free){
            unregisterFighter(fighters.get(0).uuid);
            unregisterFighter(fighters.get(1).uuid);


            tpaLobby();

            //      終了

            startEntry();
            updateSidebar();

            return 0;
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
        serverMessage("§e§l============== §d●§f●§a●§e§lMan10 Fight Club 結果速報§d●§f●§a● §e§l===============");



        serverMessage("§c勝者："+winner.getDisplayName() +"§fは§c§l優勝賞金 "+Utility.getPriceString(prize)+"§fをゲットした！！！！");
        vault.deposit(winner.getUniqueId(),prize);

        //  掛け金の計算
        double total  = getTotalBet();
        double winBet = getFighterBets(fighterIndex);

        showTitle("§cMFC Winner: "+winner.getName(),"獲得賞金:§c§l"+Utility.getPriceString(prize),5,0);

        //    オッズとは
        //  （賭けられたお金の合計 － 経費）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - getCost()) / winBet;

        for (BetInformation bet : bets) {
            PlayerInformation f = fighters.get(bet.fighterIndex);

            if (bet.fighterIndex != fighterIndex) {
                data.createBet(fightId, bet.buyerUUID, bet.bet, false, f.uuid, odds, bet.bet * -1);
                continue;
            }
            //      プレイヤーへの支払い金額
            double playerPayout = bet.bet * odds;

            //      プレイヤーへ支払い
            serverMessage("§e" + bet.buyerName + "§fは、予想があたり、" + Utility.getPriceString(playerPayout) + "§fをゲットした！！ §b§l倍率:" + String.format("%.3f", odds)+"倍");

            //      通知
            vault.deposit(bet.buyerUUID, Math.floor(playerPayout));


            //      データベース登録

            double profit = playerPayout - bet.bet;
            //  非同期で登録
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                data.createBet(fightId, bet.buyerUUID, bet.bet, true, f.uuid, odds, profit);
            });
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


    String  prefix = "§f§l[§d§lM§f§lF§a§lC§l§f§l]";

    int      entryTimer = 0;
    int      betTimer = 0;
    int      fightTimer = 0;

    int      entryTimerDefault = 30;
    int      fightTimerDefault = 120;
    int      betTimerDefault = 30;


    public void resetEnetryTimer(){

        entryTimer = entryTimerDefault;

    }
    public void resetFightTimer(){

        fightTimer = fightTimerDefault;

    }
    public void resetBetTimer(){
        betTimer = betTimerDefault;
    }

    void _showLifeBarToAll(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            lifebar.addPlayer(player);

        }

    }

    void showLifeBar(){
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
    void closeInfoBar(){
        lifebar.clearInfoBar();
    }
    public void onTickTimer(){
        //updateSigns();

    }
    public String getModeText(){


        entryTimerDefault = 30;
        fightTimerDefault = 120;
        betTimerDefault = 30;


        String ret = "";
        if(mode == MFCModes.Normal){
            ret = "§a§lMFC";
        }
        if(mode == MFCModes.Pro){
            ret = "§4§l【MFC Pro】";
            fightTimerDefault = 300;
        }
        if(mode == MFCModes.Free){
            ret = "§dFree";
            entryTimerDefault = 10;
        }
        if(mode == MFCModes.WhiteList){
            ret = "§f§lWhitelist";
        }

        return ret;
    }


    public void updateInfoBar(){

        if(currentStatus == Entry){
            if(waiters.size() == 0){
                lifebar.setInfoName(getModeText()+ " §f§l選手登録受付中!! §e§l参加費:"+ Utility.getPriceString(entryPrice));
            }else{
                lifebar.setInfoName(getModeText()+ " §a§l選手登録受付中!§e§l("+waiters.size()+") §4§l Time:"+entryTimer);
            }

            double d = (double)entryTimer / (double)entryTimerDefault;
            if(d < 0){
                d = 0;
            }
            if(d > 1){
                d = 1;
            }
            lifebar.setInfoBar(d);
        }

        if(currentStatus == Opened){
            lifebar.setInfoName(getModeText()+"§f§lベット受付中! §b"+selectedArena + "§f/§a"+selectedKit+" §4§l Time:"+betTimer+ " §e§l賞金:"+ Utility.getPriceString( getPrize()));
            double d = (double)betTimer / (double)betTimerDefault;
            if(d < 0){
                d = 0;
            }
            if(d > 1){
                d = 1;
            }
            lifebar.setInfoBar(d);

            PlayerInformation f0 = fighters.get(0);
            PlayerInformation f1 = fighters.get(1);
            double o0 = getFighterOdds(f0.uuid);
            double o1 = getFighterOdds(f1.uuid);

            String s0 = String.format("§4§l" + f0.name+": 倍率:%.3f倍",o0);
            String s1 = String.format("§5§l" + f1.name+": 倍率:%.3f倍",o1);
            lifebar.setRname(s0);
            lifebar.setBname(s1);
            lifebar.setVisible(true);
        }
        if(currentStatus == Fighting){
            lifebar.setInfoName(getModeText() + " 対戦中! §b"+selectedArena + "§f/§a"+selectedKit+" §e§l"+Utility.getPriceString( getPrize())+"§4§l Time:"+fightTimer);
            double d = (double)fightTimer / (double)fightTimerDefault;
            lifebar.setInfoBar(d);
        }

    }


    public void onTimer(){
        if(pauseTimer){
            return;
        }
      //  log("onTimer");
        updateInfoBar();
        if (currentStatus == Entry) {
            if(waiters.size() >= 2){
                entryTimer --;
                if(entryTimer <= 0){
                    openGame();
                }
            }
        //    updateSidebar();
        }
        if (currentStatus == Opened) {
            //             serverMessage("timer opened" + betTimer);
            betTimer--;

            if(betTimer <= 0){
                startGame();
            }
         //   updateSidebar();
           // updateLifeBar();
        }

        if (currentStatus == Fighting) {
            fightTimer--;
            if(fightTimer <= 0){
                serverMessage("タイムアウト！！！");
                cancelGame();
            }

       //     updateSidebar();
            updateLifeBar();
        }
    }


     String fighterInfo(FightClub.PlayerInformation f){
        String s = "§9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/ "+ Utility.getPriceString(f.prize);
        return s;
    }
    //          描画系
    private static final String[] suffix = new String[]{"","K", "M", "B", "T"};
    private static final int MAX_LENGTH = 4;

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
        sideBar = new FightClubSideBar(this);
        vault = new VaultManager(this);


        this.saveDefaultConfig();
        this.loadConfig();



        //   ワールド名チェック
        var mfcWorld = Bukkit.getWorld(worldName);
        if(mfcWorld == null){
            log("ワールド名:"+worldName + "が存在しないのでシャットダウンします");
            return;
        }


        getServer().getPluginManager().registerEvents (this,this);

        //
       getCommand("mfc").setExecutor(new FightClubCommand(this));
       getCommand("mfca").setExecutor(new FightClubArenaCommand(this));

        sideBar.showToAll();
        updateSidebar();

        //data = new FightClubData(this);


        Bukkit.getScheduler().runTaskTimer(this, this::onTimer, 0, 20);

        Bukkit.getScheduler().runTaskTimer(this, this::onTickTimer, 0, 1);


        showInfoBarToPlayer();

    }

    void showInfoBarToPlayer(){
        for(Player p : Bukkit.getOnlinePlayers()){
            showInfoBar(p);
        }
    }
    void showInfoBar(Player p){
        lifebar.addInfoPlayer(p);
        lifebar.setVisible(true);
    }
    void showLifeBar(Player p){
        lifebar.addPlayer(p);
    }

    void loadConfig(){
        loadArenaConfig();
        loadSignes();

        //      MYSQL初期化
        data = new FightClubData(this);

        boolean flag = getConfig().getBoolean("Disabled");
        if(flag){
            currentStatus = Closed;
        }

        int fee = getConfig().getInt("fee");
        int autobet = getConfig().getInt("autobet");

        this.worldName = getConfig().getString("worldName");
        this.autoBetPrice = (double)autobet;
        this.entryPrice = fee;

        this.tax = getConfig().getDouble("tax",0);
        this.prize = getConfig().getDouble("prize",0.05);
        this.newbiePlayableCount =  getConfig().getInt("newbiePlayableCount",10);
        this.registerKDRLimit  = getConfig().getDouble("registerKDRLimit",0.2);

        serverMessage("賞金比率:"+this.prize);
        serverMessage("ニュービープレイ可能回数:"+this.newbiePlayableCount);
        serverMessage("KDRリミット:"+this.registerKDRLimit);
        serverMessage("自動ベット金額:"+this.autoBetPrice);
        serverMessage("エントリ金額:"+this.entryPrice);
        updateSidebar();

        whitelist = new FightClubList("whitelist");
        blacklist = new FightClubList("blacklist");
        prolist = new FightClubList("prolist");

    }


    /////////////////////////////////
    //      終了
    /////////////////////////////////
    @Override
    public void onDisable() {
        getLogger().info("Disabled");
        cancelGame();
        lifebar.clearBar();
        lifebar.clearInfoBar();
    }


    /////////////////////////////////
    //     ジョインイベント
    /////////////////////////////////
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();

        if(currentStatus == Closed) {
        }else{
            showInfoBar(p);
            showLifeBar(p);
        }

        if(currentStatus != Fighting){
            return;
        }

        //      アリーナでなければ
        if(!p.getWorld().getName().equalsIgnoreCase(worldName)){
            return;
        }

        sideBar.addPlayer(p);
        updateSidebar();
//        tpLobby(p);
    }
    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e){
        if(currentStatus != Closed){
            if(e.getEntity().getWorld().getName().equalsIgnoreCase("arena")){
                Entity en = e.getEntity();
                if(en.getType() == EntityType.PLAYER || en.getType() == EntityType.ARROW || en.getType() == EntityType.SPLASH_POTION || en.getType() == EntityType.FISHING_HOOK || en.getType() == EntityType.FALLING_BLOCK || en.getType() == EntityType.SNOWBALL || en.getType() == EntityType.EGG || en.getType() == EntityType.ENDER_PEARL){
                }else{
                    if(currentStatus == Fighting ){


                    }


 //                   e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        log(p.getName() + "ログアウト");

        if(p.isOnline()){
            if(p.getLocation().getWorld().getName().equalsIgnoreCase(worldName)){
                p.setGameMode(GameMode.SURVIVAL);
                tpLobby(p);
                p.sendMessage("ロビーに転送された");
            }
        }

        if(unregisterFighter(p.getUniqueId()) ){
            serverMessage(p.getName() + "はログアウトしたため、登録リストからはずされた");
            updateSidebar();
        }

        for (PlayerInformation fighter : fighters) {
            if (fighter.uuid == p.getUniqueId()) {
                serverMessage(p.getName() + "はログアウトしたため、試合をキャンセルします");
                cancelGame();
                break;
            }
        }


    }


    /////////////////////////////////
    //      チャットイベント
    /////////////////////////////////
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(currentStatus != Fighting){
            return;
        }

        Player p = e.getPlayer();
        String message = e.getMessage();
                //  GlowAPI.setGlowing(e.getPlayer(), GlowAPI.Color.AQUA, Bukkit.getOnlinePlayers())
        //      対象アリーナでなければ
        if(!p.getWorld().getName().equalsIgnoreCase(worldName)){
            return;
        }
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
        //      対象アリーナでなければ
        if(!p.getWorld().getName().equalsIgnoreCase(worldName)){
            return;
        }

        //      ファイターでなければ無視
        int index = getFighterIndex(p.getUniqueId());
        if(index == -1){
            return;
        }

        PlayerInformation f = fighters.get(index);      //  死亡者
        fighters.get(index).isDead = true;

        serverMessage("死亡!!!:"+p.getDisplayName());

        int lastIndex = getLastFighter();               //  最後の生存者ID

        //      生存者
        Player pa = Bukkit.getPlayer(fighters.get(lastIndex).uuid);


        //      死亡者をよみがえらせTPさせる

        try {
            p.spigot().respawn();

        }catch (Exception exception){
            Bukkit.getLogger().info(exception.getMessage());
        }

        resetPlayerStatus(p);
        tpLobby(p);

        //      遅延実行で装備をリセット
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                resetPlayerStatus(p);
                command("man10kit pop " + p.getName());
                resetPlayerStatus(pa);
                command("man10kit pop " + pa.getName());
            }

        }, 20);

        ////////////////////////////////////
        //      最後ならゲームを終了する
        ////////////////////////////////////
        if(getAliveFighterCount() <= 1){
            serverMessage("ゲーム終了！！！");
            for(PlayerInformation pf : fighters){
                Player pn = Bukkit.getPlayer(pf.uuid);
                resetPlayerStatus(pn);
                //command("mkit pop "+pf.name );
            }
            tpaLobby();
            updateSidebar();
            endGame(lastIndex);
        }


    }


    //      銃や弓などのダメージイベント
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if(currentStatus == Closed){
            return ;
        }

        if(e.getDamager() instanceof Projectile && e.getEntity() instanceof Player) {
            //make sure the damager is a snowball & the damaged entity is a Player
            ProjectileSource shooter = ((Projectile) e.getDamager()).getShooter();

            if (!(shooter instanceof Player)) {
                return;
            }


            Player p = (Player)shooter;

            //      対象アリーナでなければ
            if(!p.getWorld().getName().equalsIgnoreCase(worldName)){
                return;
            }
            if(p.isOp()){
                return;
            }

            if(!isFighter(p.getUniqueId())){
                p.sendMessage("選手以外の戦闘行動は禁止されています");
                e.setCancelled(true);
                return;
            }
            //      試合中以外はキャンセル
            if(currentStatus != Fighting){
                e.setCancelled(true);
                return ;
            }
            //  ライフバー更新
            updateLifeBar();

        }

    }
    //      ヒットダメージ等
    @EventHandler
    public void PlayerDamageReceive(EntityDamageByEntityEvent e) {
        if(currentStatus == Closed){
            return ;
        }


        if(e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();

            //      対象アリーナでなければ
            if(!damaged.getWorld().getName().equalsIgnoreCase(worldName)){
                return;
            }

            if(!(e.getDamager() instanceof Player)){

                return ;
            }

            Player damager = (Player) e.getDamager();
            //
            if(!isFighter(damager.getUniqueId())){
                damager.sendMessage("選手以外の戦闘行動は禁止されています");
                e.setCancelled(true);
                return;
            }
            //      試合中以外はキャンセル
            if(currentStatus != Fighting){
                e.setCancelled(true);
                return ;
            }
            //  ライフバー更新
            updateLifeBar();
        }
    }

    int  getScore(FightClub.PlayerInformation inf){
        double d = inf.prize /  (double)(inf.kill + inf.death) * 0.001;
        return (int)d;
    }

    //      ログメッセージ
    void log(String text){
        getLogger().info("[MFC]:"+text);
    }

    //     サーバーメッセージ
    void serverMessage(String text){

        var mfcWorld = Bukkit.getWorld(worldName);
        if(mfcWorld == null){
            log("ワールド名:"+worldName + "が存在しない");
            return;
        }


        if( mode == MFCModes.Pro){
            Bukkit.getServer().broadcastMessage(prefix +  text);
            return;
        }
        for(Player p :mfcWorld.getPlayers()){
            p.sendMessage(text);
        }

    }
    void playerMessage(Player p,String text){
        p.sendMessage(prefix + text);
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
                //      先頭に_がつくキットは除外する
                if(filename.substring(0,1).equalsIgnoreCase("_")){
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
    public void playerChangeWorldEvent(PlayerChangedWorldEvent e){
        if(currentStatus != Closed){
            Player p = e.getPlayer();
            if(isFighter(p.getUniqueId())){
                p.sendMessage(prefix + "選手はワールド変更できません");
                tpLobby(p);
            }
            if(p.getGameMode() == GameMode.SPECTATOR){
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage(prefix + "ワールド変更されたため、観戦を終了しました");
                if(!p.isOp()){
                    tpLobby(p);
                }
            }
        }
    }
    @EventHandler
    public void commandCancel(PlayerCommandPreprocessEvent e) {
        if (currentStatus == Status.Opened || currentStatus == Status.Fighting) {
            Player p = e.getPlayer();
            if (isFighter(p.getUniqueId())) {
                if (p.isOp()) {
                    return;
                }
                p.sendMessage(prefix + "戦闘中はコマンドの使用はできません");
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void clickSignEvent(PlayerInteractEvent e) {

        if(currentStatus == Closed){
            return;
        }

        if( e.getAction() == Action.RIGHT_CLICK_BLOCK  || e.getAction() == Action.LEFT_CLICK_BLOCK ) {
        if (e.getClickedBlock().getType() == Material.OAK_SIGN || e.getClickedBlock().getType() == Material.OAK_WALL_SIGN) {
                Object o = e.getClickedBlock().getState();
                if(!(o instanceof Sign)){
                    return;
                }

                Sign s = (Sign) e.getClickedBlock().getState();
                Player p = e.getPlayer();
                if (!s.getLine(0).equalsIgnoreCase("[MFC]")){
                    return;
                }
                if (s.getLine(1).equalsIgnoreCase("BET")) {
                    if(!p.hasPermission(betPermision)){
                        p.sendMessage("ベットする権限がありません");
                        return;
                    }
                    guiBetMenu(p);
                    return;
                }
                if (s.getLine(1).equalsIgnoreCase("Register")) {
                    if(!p.hasPermission(registerPermision)){
                        p.sendMessage("選手登録する権限がありません");
                        return;
                    }


                    registerFighter(p,e.getPlayer().getUniqueId(),e.getPlayer().getName());
                    return;
                }
            if (s.getLine(1).equalsIgnoreCase("UnRegister")) {
                if(!p.hasPermission(unregisterPermision)){
                    p.sendMessage("選手登録解除する権限がありません");
                    return;
                }
                if(unregisterFighter(e.getPlayer().getUniqueId())){
                    p.sendMessage("選手登録解除しました");
                }
                return;
            }

                if (s.getLine(1).equalsIgnoreCase("Entry")) {
                    if(!p.hasPermission(adminPermision)){
                        p.sendMessage("管理者権限がありません");
                        return;
                    }
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
                    if(!p.hasPermission(adminPermision)){
                        p.sendMessage("管理者権限がありません");
                        return;
                    }
                    cancelGame();
                    return;
                }
                if (s.getLine(1).equalsIgnoreCase("Open")) {
                    if(!p.hasPermission(adminPermision)){
                        p.sendMessage("管理者権限がありません");
                        return;
                    }

                    openGame();
                    return;
                }
                if (s.getLine(1).equalsIgnoreCase("Fight")) {
                    if(!p.hasPermission(adminPermision)){
                        p.sendMessage("管理者権限がありません");
                        return;
                    }
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
    public void closeInventoryEvent(InventoryCloseEvent e){
        if(currentStatus == Closed){
            return;
        }
        gui.removeInMenu((Player) e.getPlayer());
    }


    @EventHandler
    public void clickItem(InventoryClickEvent e) {
        /*if(currentStatus == Closed){
            return;
        }*/
        if(e.getClickedInventory() == e.getWhoClicked().getInventory()){
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
    ArrayList<Location> kitSigns = new ArrayList<>();
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
            if (b.getRelative(f).getType() == Material.OAK_WALL_SIGN) {
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
    FightClubSideBar sideBar = null;
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
        for (String arena : arenas) {
            if (arena.equalsIgnoreCase(selectedArena)) {
                p.sendMessage(arena + ":(selected)");
            } else {
                p.sendMessage(arena);
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
            fixTpBug(p);
        }
        return;
    }
    public void tpf(String arena,String name){
        Object o =  getConfig().get(arena+ ".pos."+name);
        if(o != null){
            Location loc = (Location)o;
            for(PlayerInformation f :waiters){
                Player p = Bukkit.getPlayer(f.uuid);
                p.teleport(loc);
                fixTpBug(p);
            }

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
    //      選手をアリーナへ移動（スペクテータにする）
    public void tpWaiterToArena(){
        int n = 0;
        for(PlayerInformation inf : waiters){
            Player p = Bukkit.getPlayer(inf.uuid);

            if(!p.isOnline()){
                continue;
            }
            p.setGameMode(GameMode.SPECTATOR);

            if((n % 2) == 0){
                tp(p,selectedArena,"player1");
            }else{
                tp(p,selectedArena,"player2");
            }
            n++;
        }

    }

    //      アリーナにいる全員をロビーに移動
    public void tpaLobby(){
        Object o =  getConfig().get("lobby");
        if(o != null){
            Location loc = (Location)o;
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                if(player.getLocation().getWorld().getName().equalsIgnoreCase(worldName)){
                    player.teleport(loc);
                    player.setGameMode(GameMode.SURVIVAL);
                    fixTpBug(player);
                }
            }
        }
    }
    public void tpLobby(Player p){
        Object o =  getConfig().get("lobby");
        if(o != null){
            Location loc = (Location)o;
            p.teleport(loc);
            fixTpBug(p);
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
    /*
    public void itemSpawn(ItemSpawnEvent e) {
        if(currentStatus == Closed){
            return;
        }

        if(e.getLocation().getWorld().getName().equalsIgnoreCase(worldName)){


            //      試合中である
            if(currentStatus == Fighting){
                if(isFighter())


            }


            e.getEntity().remove();

        }
    }
    */


    public void onItemDrop (PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if(currentStatus == Closed){
            return;
        }


        if(p.getLocation().getWorld().getName().equalsIgnoreCase(worldName)) {

            if(currentStatus == Fighting){

                //      選手である＆
                if(isFighter(p.getUniqueId())){
                    return;
                }
            }



            p.sendMessage("MFC中はアイテムは捨てられない！！");
            e.setCancelled(true);

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

    private void updateEntities(Player tpedPlayer, List<Player> players) {
        // Hide or show every player to tpedPlayer
        // and hide or show tpedPlayer to every player.
        for (Player player : players) {
            tpedPlayer.hidePlayer(player);
            player.hidePlayer(tpedPlayer);

            tpedPlayer.showPlayer(player);
            player.showPlayer(tpedPlayer);

        }
    }


    // TODO: 本当にいまのバージョンで必要か確かめる。不要なら削除
    void fixTpBug(Player tpedPlayer){
        for (Player player: Bukkit.getWorld(worldName).getPlayers()) {
            tpedPlayer.hidePlayer(this,player);
            player.hidePlayer(this,tpedPlayer);
        }
        for (Player player: Bukkit.getWorld(worldName).getPlayers()) {
            tpedPlayer.showPlayer(this,player);
            player.showPlayer(this,tpedPlayer);
        }
    }

}
