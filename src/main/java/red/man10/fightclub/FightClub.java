package red.man10.fightclub;




import co.insou.skulls.SkullMaker;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import static red.man10.fightclub.FightClub.Status.*;
import static sun.audio.AudioPlayer.player;


public final class FightClub extends JavaPlugin implements Listener {

    //   状態遷移 これらの状態遷移する
    public enum Status {
        Closed,                 //  開催前
        Entry,                  //  募集中
        Opened,                  // 予想の受付開
        Fighting,                //  対戦中
    }

    //      プレーヤ情報
    class FighterInformation {
        UUID uuid;
        String name;
        Boolean isDead;
    }

    //      購入者情報
    class BuyerInformation {
        UUID uuid;
        String name;
    }

    //      賭け情報
    class BetInformation {
        UUID buyerUUID;       //  購入者のUUID
        String buyerName;       //  購入者の名前
        int fighterIndex;     //  プレーヤ情報
        double bet;             //  掛け金
    }

    double tax = 0;
    Status currentStatus = Closed;

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
    public int registerFighter(UUID uuid, String name) {

        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for (int i = 0; i < filghters.size(); i++) {
            FighterInformation fighter = filghters.get(i);
            if (fighter.uuid == uuid) {
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
        for (int i = 0; i < buyers.size(); i++) {
            if (buyers.get(i).uuid == uuid) {
                return i;
            }
        }
        return -1;
    }

    //
    int getFighterIndex(UUID uuid) {
        for (int i = 0; i < filghters.size(); i++) {
            if (filghters.get(i).uuid == uuid) {
                return i;
            }
        }
        return -1;
    }

    //      生存者数
    int getAliveFighterCount() {
        int ret = 0;
        for (int i = 0; i < filghters.size(); i++) {
            if (filghters.get(i).isDead == false) {
                ret++;
            }
        }
        return ret;
    }

    //      生存者数
    int getLastFighter() {
        int ret = 0;
        for (int i = 0; i < filghters.size(); i++) {
            if (filghters.get(i).isDead == false) {
                return i;
            }
        }
        return -1;
    }


    double getFighterBetMoney(UUID uuid) {


        int index = getFighterIndex(uuid);
        if (index == -1) {
            return 0;
        }
        return getFighterBets(index);

    }

    //      購入された数
    int getFighterBetCount(UUID uuid) {
        int index = getFighterIndex(uuid);
        if (index == -1) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < bets.size(); i++) {
            BetInformation bet = bets.get(i);
            if (bet.fighterIndex == index) {
                count++;
            }
        }
        return count;
    }

    //////////////////////
    //      odds
    //////////////////////
    double getFighterOdds(UUID uuid) {

        //      購入された金額
        double bet = getFighterBetMoney(uuid);
        double total = getTotalBets();
        if (bet == 0) {
            return 1.0;
        }
        //  （賭けられたお金の合計 － 手数料）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - tax) / bet;
        return odds;
    }

    double getFighterBets(int fighterIndex) {
        double totalBet = 0;
        for (int i = 0; i < bets.size(); i++) {
            BetInformation bet = bets.get(i);
            if (bet.fighterIndex == fighterIndex) {
                totalBet += bet.bet;
            }
        }
        return totalBet;
    }

    ///////////////////////////////////
    //      トータル掛け金
    ///////////////////////////////////
    double getTotalBets() {
        double totalBet = 0;
        for (int i = 0; i < bets.size(); i++) {
            totalBet += bets.get(i).bet;
        }
        return totalBet;
    }

    //////////////////////////////////////////////
    //     プレイーやに賭ける 成功なら掛け金テーブルindex
    //////////////////////////////////////////////
    int betFighter(UUID fighterUUID, double price, UUID buyerUUID, String buyerName) {

        int index = getFighterIndex(fighterUUID);
        if (index == -1) {
            return -1;
        }

        /////////////////////////////////////////
        //     同じ相手への購入ならbetをマージ
        /////////////////////////////////////////
        for (int i = 0; i < bets.size(); i++) {
            BetInformation bet = bets.get(i);
            //      同じ購入IDのみ
            if (bet.buyerUUID != buyerUUID) {
                continue;
            }
            if (bet.fighterIndex == index) {
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
    int cancelGame(Player p) {

        //   払い戻し処理
        for (int i = 0; i < bets.size(); i++) {
            BetInformation bet = bets.get(i);
            p.sendMessage("Return money to " + bet.buyerName + " $" + bet.bet);

            this.deposit(bet.buyerUUID, bet.bet);
            Bukkit.getPlayer(bet.buyerName).sendMessage("ゲームがキャンセルされお金を$" + bet.bet + "返金しました。");
        }
        bets.clear();
        filghters.clear();
        buyers.clear();
        currentStatus = Closed;
        return 0;
    }

    //      募集開始
    public int openGame() {
        currentStatus = Opened;
        return 0;
    }

    //      ゲーム開始
    public int startGame() {
        currentStatus = Fighting;
        return 0;
    }

    //      対戦終了　winPlayer = -1 終了
    public int endGame(Player p, int fighterIndex) {
        if (fighterIndex == -1) {
            return cancelGame(p);
        }

        //  掛け金の計算
        double total = getTotalBets();
        double winBet = getFighterBets(fighterIndex);


        //    オッズとは
        //  （賭けられたお金の合計 － 手数料）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - tax) / winBet;

        for (int i = 0; i < bets.size(); i++) {
            BetInformation bet = bets.get(i);
            if (bet.fighterIndex != fighterIndex) {
                continue;
            }
            //      プレイヤーへの支払い金額
            double playerPayout = bet.bet * odds;
            //      プレイヤーへ支払い
            serverMessage(bet.buyerName + "は, 元金額:$" + bet.bet + "-> $" + playerPayout + "Odds x" + odds);

            //      通知
            this.deposit(bet.buyerUUID, playerPayout);

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
    String mysql_ip;
    String mysql_port;
    String mysql_user;
    String mysql_pass;
    String mysql_db;

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////
    public void loadConfig() {
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
        getServer().getPluginManager().registerEvents(this, this);

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
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage(ChatColor.YELLOW + "Man10 Fight Club System Started.");


    }

    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    double getBalance(UUID uuid) {
        return economy.getBalance(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }

    /////////////////////////////////////
    //      引き出し
    /////////////////////////////////////
    Boolean withdraw(UUID uuid, double money) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        EconomyResponse resp = economy.withdrawPlayer(p, money);
        if (resp.transactionSuccess()) {
            return true;
        }
        return false;
    }

    /////////////////////////////////////
    //      お金を入れる
    /////////////////////////////////////
    Boolean deposit(UUID uuid, double money) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        EconomyResponse resp = economy.depositPlayer(p, money);
        if (resp.transactionSuccess()) {
            serverMessage("振込成功" + p.getName() + " $" + money);
            return true;
        }
        serverMessage("振込失敗" + p.getName() + " $" + money);
        return false;
    }

    /////////////////////////////////
    //      チャットイベント
    /////////////////////////////////
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        String message = e.getMessage();
        serverMessage("test");


        //ItemStack head = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/7c57f9192e81eb6897c24ecd4935cfb5a731a6f9a57abb51f2b35e8b4be7ebc").build();


        // CustomSkullAPI.createSkull("http://textures.minecraft.net/texture/7c57f9192e81eb6897c24ecd4935cfb5a731a6f9a57abb51f2b35e8b4be7ebc");

        //p.getInventory().addItem(Skull.getCustomSkull("http://textures.minecraft.net/texture/7c57f9192e81eb6897c24ecd4935cfb5a731a6f9a57abb51f2b35e8b4be7ebc");

        //p.getInventory().addItem(head);

        //p.sendMessage(ChatColor.YELLOW + message );

/*
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
*/

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
        Player p = (Player) e.getEntity();


        //      死亡フラグを立てる
        int index = getFighterIndex(p.getUniqueId());
        if (index != -1) {
            filghters.get(index).isDead = true;
            serverMessage("死亡!!!:" + p.getDisplayName());


            //      最後ならゲームを終了する
            if (getAliveFighterCount() <= 1) {
                serverMessage("ゲーム終了！！！");

                int lastIndex = getLastFighter();
                endGame(p, lastIndex);
                return;

            } else {
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
    public void onHit(EntityDamageEvent e) {


        // serverMessage("damage :" +e.getDamage());

        Player p = (Player) e.getEntity();
        // if(e.getEntity() instanceof Player)
        {
            //  serverMessage("instance player");
            //    command("say damage"+e.getDamage()+e.getCause()+e.getFinalDamage());

            // p.setScoreboard(board);

        }
        ///   String s = "生存者/プレーヤ= " + getAliveFighterCount() + "/" + filghters.size();
        ///  serverMessage(s);
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

    void createTables() {
        executeSQL(sqlCrateChatLogTable);
    }

    ////////////////////////////////
    //      SQL実行
    ////////////////////////////////
    Boolean executeSQL(String sql) {
        // getLogger().info("executing SQL" + sql);
        Connection conn;
        try {
            //      データベース作成
            Class.forName("com.mysql.jdbc.Driver");
            String databaseURL = "jdbc:mysql://" + mysql_ip + "/" + mysql_db;
            //getLogger().info(databaseURL);

            conn = DriverManager.getConnection(databaseURL, mysql_user, mysql_pass);
            Statement st = conn.createStatement();
            st.execute(sql);

            st.close();
            conn.close();
            //getLogger().info("SQL performed");
            return true;
        } catch (ClassNotFoundException e) {
            getLogger().warning("Could not read driver");
        } catch (SQLException e) {
            getLogger().warning("Database connection error");
        }
        return false;
    }

    //      ログメッセージ
    void log(String text) {
        getLogger().info(text);
    }

    //     サーバーメッセージ
    void serverMessage(String text) {
        //command("say "+text);
        Bukkit.getServer().broadcastMessage(text);
    }

    void titleMessage(Player p, String title, String subTitle) {

    }

    @EventHandler
    public void clickSignEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
                Sign s = (Sign) e.getClickedBlock().getState();
                if (s.getLine(0).equalsIgnoreCase("[MFC]")) {
                    if (s.getLine(1).equalsIgnoreCase("BET")) {
                        //
                        betMenu(e.getPlayer());

                    }
                }
            }
        }
    }


    @EventHandler
    public void clickItem(InventoryClickEvent e) {
        //try {
        if (e.getClickedInventory() != null) {
            if (e.getInventory().getName().equalsIgnoreCase("§c§l         ベットメニュー")) {
                Player p = (Player) e.getWhoClicked();
                FightClub.FighterInformation info = filghters.get(0);
                FightClub.FighterInformation info1 = filghters.get(1);
                if (e.getCurrentItem().getType() == Material.SKULL_ITEM) {
                    priceMenu(p, e.getCurrentItem().getItemMeta().getDisplayName());
                    e.setCancelled(true);
                } else {
                    e.setCancelled(true);
                }

            } else if (e.getInventory().getItem(52).getItemMeta().getDisplayName().equalsIgnoreCase("§c§lキャンセル")) {
                Player p = (Player) e.getWhoClicked();
                String val = e.getClickedInventory().getItem(50).getItemMeta().getLore().get(1);
                if (val.length() <= 8) {
                    if (e.getSlot() == 46) { //0
                        if(e.getInventory().getItem(50).getItemMeta().getLore().get(1).length() > 0){
                            moveD(e.getClickedInventory());
                            createDisplay(e.getClickedInventory(), p, 0);
                        }
                    } else if (e.getSlot() == 37) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 1);
                    } else if (e.getSlot() == 38) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 2);
                    } else if (e.getSlot() == 39) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 3);
                    } else if (e.getSlot() == 28) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 4);
                    } else if (e.getSlot() == 29) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 5);
                    } else if (e.getSlot() == 30) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 6);
                    } else if (e.getSlot() == 19) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 7);
                    } else if (e.getSlot() == 20) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 8);
                    } else if (e.getSlot() == 21) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 9);
                    } else if (e.getSlot() == 48) {
                        clearCalc(e.getClickedInventory());
                    } else if (e.getSlot() == 52) {
                        p.closeInventory();
                    } else if (e.getSlot() == 50) {
                        placeBetGUI(e.getInventory(),p);
                        //確認処理
                    }
                    e.setCancelled(true);
                }else{
                    if(e.getSlot() == 48){
                        clearCalc(e.getInventory());
                    }else if(e.getSlot() == 50){
                        placeBetGUI(e.getInventory(),p);
                    }else if(e.getSlot() == 52){
                        p.closeInventory();
                    }

                    e.setCancelled(true);
                    p.sendMessage("上限！！！");
                }
            }
        }else{
            //その他のインベントリ
        }
        //}catch (Exception ee){
    }
//e.getInventory().getItem(33).getItemMeta().getLore().set(1, e.getClickedInventory().getItem(33).getItemMeta().getLore().get(2));

    void placeBetGUI(Inventory i, Player p){
        int betMoney = Integer.parseInt(i.getItem(50).getItemMeta().getLore().get(1)); //設定したbal
        p.sendMessage("you bet " + betMoney + "!!!!");
        p.closeInventory();
        //確認処理
    }

    void clearCalc(Inventory e){
        e.setItem(0, new ItemStack(Material.AIR));
        e.setItem(1, new ItemStack(Material.AIR));
        e.setItem(2, new ItemStack(Material.AIR));
        e.setItem(3, new ItemStack(Material.AIR));
        e.setItem(4, new ItemStack(Material.AIR));
        e.setItem(5, new ItemStack(Material.AIR));
        e.setItem(6, new ItemStack(Material.AIR));
        e.setItem(7, new ItemStack(Material.AIR));
        e.setItem(8, new ItemStack(Material.AIR));



        String betp = e.getItem(33).getItemMeta().getDisplayName();
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemMeta ac = Accept.getItemMeta();
        String val = e.getItem(50).getItemMeta().getLore().get(1);

        ac.setDisplayName("§a§l確認");
        ArrayList<String> conf = new ArrayList<String>();
        conf.add("§e§l" + betp + "に");
        conf.add("");
        conf.add("§e§l賭ける");
        ac.setLore(conf);
        Accept.setItemMeta(ac);

        e.setItem(50, Accept);


    }


    void createDisplay(Inventory i,Player p,int num){
        int l = i.getItem(50).getItemMeta().getLore().get(2).length();
        ItemStack item = new ItemStack(Material.AIR);
        if(num==0){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build();
            setTextPrice(i, 0);
        }else if(num==1){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build();
            setTextPrice(i, 1);
        }else if(num==2){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build();
            setTextPrice(i, 2);
        }else if(num==3){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build();
            setTextPrice(i, 3);
        }else if(num==4){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build();
            setTextPrice(i, 4);
        }else if(num==5){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build();
            setTextPrice(i, 5);
        }else if(num==6){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build();
            setTextPrice(i, 6);
        }else if(num==7){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build();
            setTextPrice(i, 7);
        }else if(num==8){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build();
            setTextPrice(i, 8);
        }else if(num==9){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build();
            setTextPrice(i, 9);
        }

        i.setItem(8, item);
    }

    void setTextPrice(Inventory i,int num){
        String betp = i.getItem(33).getItemMeta().getDisplayName();
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemMeta ac = Accept.getItemMeta();
        String val = i.getItem(50).getItemMeta().getLore().get(1);

        ac.setDisplayName("§a§l確認");
        ArrayList<String> conf = new ArrayList<String>();
        conf.add("§e§l" + betp + "に");
        conf.add(val + num);
        conf.add("§e§l賭ける");
        ac.setLore(conf);
        Accept.setItemMeta(ac);

        i.setItem(50, Accept);

    }

    void moveD(Inventory i){
        i.setItem(0, i.getItem(1));
        i.setItem(1, i.getItem(2));
        i.setItem(2, i.getItem(3));
        i.setItem(3, i.getItem(4));
        i.setItem(4, i.getItem(5));
        i.setItem(5, i.getItem(6));
        i.setItem(6, i.getItem(7));
        i.setItem(7, i.getItem(8));
    }

    public void priceMenu(Player p,String betp) {
        //ItemStack head = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/7c57f9192e81eb6897c24ecd4935cfb5a731a6f9a57abb51f2b35e8b4be7ebc").build();
        Inventory inv = Bukkit.createInventory(null, 54, betp + "にベットする");
        ItemStack i0 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build();
        ItemStack i1 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build();
        ItemStack i2 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build();
        ItemStack i3 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build();
        ItemStack i4 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build();
        ItemStack i5 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build();
        ItemStack i6 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build();
        ItemStack i7 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build();
        ItemStack i8 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build();
        ItemStack i9 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build();

        ItemStack price = new ItemStack(Material.EMERALD, 1);
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(betp);
        meta.setOwner(betp);
        skull.setItemMeta(meta);

        ArrayList<String> conf = new ArrayList<String>();
        conf.add("§e§l" + betp + "に");
        conf.add("");
        conf.add("§e§l賭ける");

        ItemStack clear = new ItemStack(Material.TNT, 1);
        ItemMeta clearm = clear.getItemMeta();
        clearm.setDisplayName("§c§lクリア");
        clear.setItemMeta(clearm);


        ArrayList<String> a = new ArrayList<String>();


        ItemMeta am = Accept.getItemMeta();
        ItemMeta cm = cancel.getItemMeta();

        a.add("§d§l掛け金");
        am.setLore(conf);


        am.setDisplayName("§a§l確認");
        cm.setDisplayName("§c§lキャンセル");

        Accept.setItemMeta(am);
        cancel.setItemMeta(cm);


        ItemMeta i0m = i0.getItemMeta();
        ItemMeta i1m = i1.getItemMeta();
        ItemMeta i2m = i2.getItemMeta();
        ItemMeta i3m = i3.getItemMeta();
        ItemMeta i4m = i4.getItemMeta();
        ItemMeta i5m = i5.getItemMeta();
        ItemMeta i6m = i6.getItemMeta();
        ItemMeta i7m = i7.getItemMeta();
        ItemMeta i8m = i8.getItemMeta();
        ItemMeta i9m = i9.getItemMeta();

        i0m.setDisplayName("§7§l0");
        i1m.setDisplayName("§7§l1");
        i2m.setDisplayName("§7§l2");
        i3m.setDisplayName("§7§l3");
        i4m.setDisplayName("§7§l4");
        i5m.setDisplayName("§7§l5");
        i6m.setDisplayName("§7§l6");
        i7m.setDisplayName("§7§l7");
        i8m.setDisplayName("§7§l8");
        i9m.setDisplayName("§7§l9");

        i0.setItemMeta(i0m);
        i1.setItemMeta(i1m);
        i2.setItemMeta(i2m);
        i3.setItemMeta(i3m);
        i4.setItemMeta(i4m);
        i5.setItemMeta(i5m);
        i6.setItemMeta(i6m);
        i7.setItemMeta(i7m);
        i8.setItemMeta(i8m);
        i9.setItemMeta(i9m);

        inv.setItem(19, i7);
        inv.setItem(20, i8);
        inv.setItem(21, i9);

        inv.setItem(28, i4);
        inv.setItem(29, i5);
        inv.setItem(30, i6);

        inv.setItem(37, i1);
        inv.setItem(38, i2);
        inv.setItem(39, i3);

        inv.setItem(46, i0);

        inv.setItem(50, Accept);
        inv.setItem(52, cancel);
        
        inv.setItem(33, skull);

        inv.setItem(48, clear);
        //inv.setItem(38, removeone);

        p.openInventory(inv);
    }



    public void betMenu(Player p){
        FightClub.FighterInformation info = filghters.get(0);
        FightClub.FighterInformation info1 = filghters.get(1);
        Inventory betinv = Bukkit.createInventory(null,27, "§c§l         ベットメニュー");
        ItemStack ri = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)14);
        ItemMeta rim = ri.getItemMeta();
        rim.setDisplayName("§c§l" + info.name + "にベットする");
        ri.setItemMeta(rim);

        ItemStack ri1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)11);
        ItemMeta rim1 = ri1.getItemMeta();
        rim1.setDisplayName("§9§l" + info1.name + "にベットする");
        ri1.setItemMeta(rim1);

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(info.name);
        meta.setOwner(info.name);
        skull.setItemMeta(meta);

        ItemStack skull1 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta1 = (SkullMeta) skull1.getItemMeta();
        meta1.setDisplayName(info1.name);
        meta1.setOwner(info1.name);
        skull1.setItemMeta(meta1);

        betinv.setItem(0, ri);
        betinv.setItem(1, ri);
        betinv.setItem(2, ri);
        betinv.setItem(9, ri);
        betinv.setItem(11, ri);
        betinv.setItem(18, ri);
        betinv.setItem(19, ri);
        betinv.setItem(20, ri);

        betinv.setItem(10, skull);

        betinv.setItem(6, ri1);
        betinv.setItem(7, ri1);
        betinv.setItem(8, ri1);
        betinv.setItem(15, ri1);
        betinv.setItem(16, skull1);
        betinv.setItem(17, ri1);
        betinv.setItem(24, ri1);
        betinv.setItem(25, ri1);
        betinv.setItem(26, ri1);
        p.openInventory(betinv);

    }


    //      コマンド実行　
    void command(String command){
        getServer().dispatchCommand(getServer().getConsoleSender(),command);
    }
}
