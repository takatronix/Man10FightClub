package red.man10.fightclub;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import red.man10.Utility;


/**
 * Created by takatronix on 2017/03/01.
 */
public class FightClubCommand  implements CommandExecutor {
    private final FightClub plugin;

    public FightClubCommand(FightClub plugin) {
       this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player p = (Player)sender;

        //      引数がない場合
        if(args.length == 0){
            plugin.openGUI(p);
            return true;
        }



        // MFC表示を再開する
        if(args[0].equalsIgnoreCase("hide")) {
            this.plugin.addUninterested(p);
            return true;
        }
        // MFC表示を止める
        if(args[0].equalsIgnoreCase("show")) {
            this.plugin.removeUninterested(p);
            return true;
        }

        //  retry
        if(args[0].equalsIgnoreCase("retry")) {
            this.retryChallenge(p);
            return true;
        }

        if(args[0].equalsIgnoreCase("reset")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.resetUserData(sender,args[1]);
            });
            return false;
        }

        if(args[0].equalsIgnoreCase("resethistory")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.resetUserHistory(sender,args[1],false);
            });
            return false;
        }
        if(args[0].equalsIgnoreCase("resethistory.pro")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.resetUserHistory(sender,args[1],true);
            });
            return false;

        }

        if(args[0].equalsIgnoreCase("update")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            p.sendMessage("データ取得中...");
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                var pi =  this.updateUserData(sender,args[1] ,false);
                if(pi == null){
                    p.sendMessage("データ取得失敗");
                    return;
                }
                p.sendMessage(pi.getDetail());
            });
            return false;
        }
        if(args[0].equalsIgnoreCase("update.pro")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            p.sendMessage("データ取得中...");
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                var pi =  this.updateUserData(sender,args[1] ,true);
                if(pi == null){
                    p.sendMessage("データ取得失敗");
                    return;
                }
                p.sendMessage(pi.getDetail());
            });
            return false;
        }

        if(args[0].equalsIgnoreCase("data")) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                var pi = plugin.data.getPlayerDataByName(false,args[1]);
                if(pi == null){
                    p.sendMessage("データ取得失敗");
                    return ;
                }
                p.sendMessage(pi.getDetail());
            });
            return false;
        }
        if(args[0].equalsIgnoreCase("data.pro")) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

                var pi = plugin.data.getPlayerDataByName(true,args[1]);
                if(pi == null){
                    p.sendMessage("データ取得失敗");
                    return ;
                }
                p.sendMessage(pi.getDetail());
            });
            return false;
        }


        if(args[0].equalsIgnoreCase("help")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            showHelp(p);
            return true;
        }
        if(args[0].equalsIgnoreCase("clear")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.waiters.clear();
            p.sendMessage("待ちリストクリアしました");
            plugin.sideBar.show();
            return true;
        }

        //      ON/OFFコマンド
        if(args[0].equalsIgnoreCase("on")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.setMFCMode(sender,FightClub.MFCModes.Normal);
            return false;
        }
        if(args[0].equalsIgnoreCase("off")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.waiters.clear();
            plugin.setMFCMode(sender,FightClub.MFCModes.Off);
            return false;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.reload(sender);
            return false;
        }
        if(args[0].equalsIgnoreCase("autobet")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }

            if(args.length != 2) {
                p.sendMessage("/mfc autobet [money]");
                return false;
            }

            double money = Double.parseDouble(args[1]);
            plugin.autoBetPrice = money;
            plugin.serverMessage("自動ベット金額を"+ Utility.getPriceString(money)+"に設定しました");
            plugin.getConfig().set("autobet",(int)plugin.autoBetPrice);
            plugin.saveConfig();
            return false;
        }

        if(args[0].equalsIgnoreCase("fee")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }

            if(args.length != 2) {
                p.sendMessage("/mfc fee [money]");
                return false;
            }

            double money = Double.parseDouble(args[1]);
            plugin.entryPrice = (long)money;
            plugin.getConfig().set("fee",plugin.entryPrice);
            plugin.serverMessage("登録費用を"+Utility.getPriceString(money)+"に設定しました");
            plugin.saveConfig();

            return false;
        }
        if(args[0].equalsIgnoreCase("prize")) {
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }

            if(args.length != 2) {
                p.sendMessage("/mfc fee [money]");
                return false;
            }

            double prize = Double.parseDouble(args[1]);
            plugin.prize_ratio = prize;
            plugin.getConfig().set("prize",prize);
            plugin.serverMessage("賞金を"+Utility.getPriceString(prize)+"に設定しました");
            plugin.saveConfig();

            return false;
        }

        ////////////////////////////////////
        //          エントリー
        ////////////////////////////////////
        if(args[0].equalsIgnoreCase("entry")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            if (plugin.currentStatus != FightClub.Status.Closed){
                p.sendMessage("ゲームをキャンセルしてください!");
                return false;
            }

            plugin.currentStatus = FightClub.Status.Entry;

            p.sendMessage("エントリーされました");
            plugin.updateSidebar();
            return false;
        }

        ////////////////////////////////////
        //        アリーナ強制
        ////////////////////////////////////
        if(args[0].equalsIgnoreCase("arena")) {

            if (args.length != 2) {
                this.plugin.serverMessage("自動設定アリーナを初期化");
                this.plugin.fixedArena = "";
                return false;
            }
            this.plugin.fixedArena = args[1];
            this.plugin.serverMessage("強制アリーナ設定:"+this.plugin.fixedArena);
            return false;
        }


        if(args[0].equalsIgnoreCase("register")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }

            if(args.length != 2) {
                p.sendMessage("/mfc register [fighter]");
                return false;
            }

            Player fighter = Bukkit.getPlayer(args[1]);
            if (fighter == null) {
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" is offline!!");
                return false;
            }

            int ret = plugin.registerFighter(p,fighter.getUniqueId(),args[1]);
            if (ret == -1){
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +"はすでに参加してるもしくは、興味がない");
                return false;
            }
            showOdds(p);
            plugin.updateSidebar();
            return true;
        }
            if(args[0].equalsIgnoreCase("unregister")){
                if(!p.hasPermission(plugin.adminPermision)){
                    p.sendMessage("管理者権限がありません");
                    return false;
                }

            if(args.length != 2) {
                p.sendMessage("/mfc ungregister [fighter]");
                return false;
            }

            Player fighter = Bukkit.getPlayer(args[1]);
            if (fighter == null) {
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" はオフラインです");
                return false;
            }

            boolean ret = plugin.unregisterFighter(fighter.getUniqueId());
            if (ret == false){
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" はすでに登録解除されています");
                return false;
            }
         //   showOdds(p);
            plugin.updateSidebar();
            return true;
        }



        ////////////////////////////////////
        //        強制勝利
        ////////////////////////////////////
        if(args[0].equalsIgnoreCase("end")) {
            if (args.length != 2) {
                p.sendMessage("/mfc end [fighter]");
                return false;
            }
            return false;
        }

        ////////////////////////////////////
        //        強制勝利
        ////////////////////////////////////
        if(args[0].equalsIgnoreCase("end")) {
            if (args.length != 2) {
                p.sendMessage("/mfc end [fighter]");
                return false;
            }
            return false;
        }
        //////////////////////////////////
        ///       キャンセル
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("cancel")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.cancelGame();
            p.sendMessage("MFC Closed.");
            return true;
        }
        if(args[0].equalsIgnoreCase("close")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.cancelGame();
            p.sendMessage("MFC Closed.");
            return true;
        }

        if(args[0].equalsIgnoreCase("lobby")){
            plugin.teleportToLobby(p);
            return true;
        }
        if(args[0].equalsIgnoreCase("watch")){
            plugin.tp(p,plugin.selectedArena,"spawn");

            return true;
        }
        if(args[0].equalsIgnoreCase("clean")){
            p.sendMessage("clean");
            plugin.clearEntity();

            return true;}

        //////////////////////////////////
        ///       キャンセル
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("open")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限が必要です");
                return false;
            }

            String arena = null;
            String kit = null;
            if( args.length >= 2){
                arena = args[1];
            }
            if( args.length >= 3){
                kit = args[2];
            }
            plugin.openGame(sender,arena,kit);
            p.sendMessage("MFC Opened");
            return true;
        }
        if(args[0].equalsIgnoreCase("admin")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限が必要です");
                return false;
            }
            //plugin.gui.adminMenu(p);
         //   p.sendMessage("MFC Opened");
            return true;
        }
        //////////////////////////////////
        ///       ファイト
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("fight")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限が必要です");
                return false;
            }
            plugin.startGame();
            p.sendMessage("MFC Started");
            return true;
        }

        //////////////////////////////////
        ///         Bet
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("bet")){
            if( args.length < 3){
                p.sendMessage("/mfc bet [fighter] [money]");
                return false;
            }
            if(plugin.currentStatus == FightClub.Status.Fighting){
                p.sendMessage(plugin.prefix + "試合中はベットできません");
                return false;
            }
            double money = Double.parseDouble(args[2]);
            Player fighter = Bukkit.getPlayer(args[1]);
            String buyer = p.getName();
            p.sendMessage(buyer);

            double balance = plugin.vault.getBalance(p.getUniqueId());
            p.sendMessage("あなたの残額は "+Utility.getPriceString(balance) +"です");
            if(balance < money){
                p.sendMessage(ChatColor.RED+ "残高が足りません！！");
                return false;
            }
            if(money < plugin.minBetPrice){
                p.sendMessage(ChatColor.RED+ Utility.getPriceString(plugin.minBetPrice)+"以下のベットはできません");
                return false;
            }

            if(plugin.betFighter(fighter.getUniqueId(),money,p.getUniqueId(),buyer) == -1){
                p.sendMessage("Error :" + args[1] +"is not on entry!");
                return false;
            }

            if(plugin.vault.withdraw(p.getUniqueId(),money) == false){
                p.sendMessage(ChatColor.RED+ "お金の引き出しに失敗しました" );
                return false;
            }

            p.sendMessage(fighter.getName() +"へ、" + Utility.getPriceString(money) + " ベットしました！！");
            p.sendMessage(ChatColor.YELLOW + "あなたの残高は" + Utility.getPriceString(plugin.vault.getBalance(p.getUniqueId())) +"です");
          //  plugin.showSideBar(p);
            showOdds(p);
            return true;
        }
        //////////////////////////////////
        //      オッズ表示
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("odds")) {
            showOdds(p);
            return false;
        }
        //////////////////////////////////
        //     bet表示
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("bets")) {
            showBets(p);
            return false;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("whitelist") ||
                args[0].equalsIgnoreCase("free") ||
                args[0].equalsIgnoreCase("pro") ||
                args[0].equalsIgnoreCase("normal")
                ){
                String modeName = args[0];
            if(!sender.hasPermission(plugin.adminPermision)){
                sender.sendMessage("管理者権限がありません");
                return false;
            }

            FightClub.MFCModes m = FightClub.MFCModes.Off;
            if(args[0].equalsIgnoreCase("whitelist")){
                m = FightClub.MFCModes.WhiteList;
            }
            if(args[0].equalsIgnoreCase("free")){
                m = FightClub.MFCModes.Free;
            }
            if(args[0].equalsIgnoreCase("pro")){
                m = FightClub.MFCModes.Pro;
            }
            if(args[0].equalsIgnoreCase("normal")){
                m = FightClub.MFCModes.Normal;
            }
            plugin.setMFCMode(sender,m);
            return true;
        }


        //////////////////////////////////
        ///         Bet
        //////////////////////////////////
        if( args[0].equalsIgnoreCase("whitelist") ||
            args[0].equalsIgnoreCase("blacklist") ||
            args[0].equalsIgnoreCase("prolist") ) {
            listCommand(sender,args);
            return true;
        }
        p.sendMessage("invalid command");

        return true;
    }


    boolean listCommand(CommandSender s,String[] args){

     //   s.sendMessage("args :"+args.length);
        String name = args[0];


        FightClubList list = null;
        if(args[0].equalsIgnoreCase("whitelist")){
            list = plugin.whitelist;
        }
        if(args[0].equalsIgnoreCase("blacklist")){
            list = plugin.blacklist;
        }
        if(args[0].equalsIgnoreCase("prolist")){
            list = plugin.prolist;
        }

        if(args.length < 3){
            list.print(s);
            return true;
        }
        if(!s.hasPermission(plugin.adminPermision)){
            s.sendMessage("管理者権限がありません");
            return false;
        }

        String target = args[2];
        Player p = Bukkit.getPlayer(target);

        ///     追加
        if(args[1].equalsIgnoreCase("add")){
            list.add(s,p);
        }
        if(args[1].equalsIgnoreCase("delete")){
            list.delete(s,p);
        }
        return true;
    }



    void showBets(Player p){

        p.sendMessage("§e=========== §d●§f●§a●§e Man10 Fight Club Buyer §d●§f●§a● §e===============");
        for(int i=0;i < plugin.bets.size();i++){
            FightClub.BetInformation info = plugin.bets.get(i);

            double price = info.bet;
            String fighter =   plugin.fighters.get(info.fighterIndex).name;

            p.sendMessage("["+i+"]:" +info.buyerName +"   $"+price +" fighter:"+fighter);
        }
        p.sendMessage("-------------------------");
        p.sendMessage("total: $"+plugin.getTotalBet());

    }

    void showOdds(Player p){

        p.sendMessage("§e=========== §d●§f●§a●§e Man10 Fight Club Odds §d●§f●§a● §e===============");
        for(int i=0;i < plugin.fighters.size();i++){
            PlayerInformation info = plugin.fighters.get(i);
         //   Player fighter = Bukkit.getPlayer(info.uuid);

            double price = plugin.getFighterBetMoney(info.uuid);
            int count = plugin.getFighterBetCount(info.uuid);

            double odds = plugin.getFighterOdds(info.uuid);
            String ods = String.format("§b§l倍率:%.2f倍",odds);

            p.sendMessage("["+i+"]:" +info.name +" Death:"+ info.isDead +"   $"+price +"  Count:"+count+" "+ods);
        }
        p.sendMessage("-------------------------");
        p.sendMessage("total: $"+plugin.getTotalBet());

    }




    void retryChallenge(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            p.sendMessage("プレイヤー情報を取得中....");
            var pi = plugin.data.getPlayerData(false,p.getUniqueId());
            p.sendMessage(pi.getInfo());

            if(pi.death == 0 ) {
                p.sendMessage("あなたは死亡履歴がないので対象外です");
                return;
            }
            if(pi.getKDR() > this.plugin.registerKDRLimit){
                p.sendMessage("あなたはのKDRは"+this.plugin.registerKDRLimit+"以上なので、対象外です");
                return;
            }
            if(!this.plugin.vault.withdraw(p.getUniqueId(),this.plugin.resetPlayerDataPrice)){
                p.sendMessage("再チャレンジには"+Utility.getPriceString(this.plugin.resetPlayerDataPrice)+"必要です");
                return;
            }

            resetUserData(p,p.getName());

            p.sendMessage("プレイヤーデータをリセットしました。１度だけ再挑戦してKDRを上げることができます");
        });
    }

    boolean resetUserData(CommandSender sender,String name){

        var ret = plugin.data.deletePlayerDataByName(false,name);
        if(ret == false){
            sender.sendMessage("削除に失敗しました");
            return false;
        }
        sender.sendMessage("削除しました");
        return true;
    }

    /**
     * 履歴データ削除
     * @param sender
     * @param name
     * @return
     */
    boolean resetUserHistory(CommandSender sender,String name,boolean isPro){
        var ret = plugin.data.deletePlayerDataByName(isPro,name);
        if(ret == false){
            sender.sendMessage("データ削除に失敗しました");
        }
        ret = plugin.data.deletePlayerHistoryByName(isPro,name);
        if(ret == false){
            sender.sendMessage("離席削除に失敗しました");
        }
        sender.sendMessage("ユーザーデータ削除しました");
        return true;
    }

    PlayerInformation updateUserData(CommandSender sender,String name,boolean isPro){
        var pi = plugin.data.getPlayerDataByName(isPro,name);
        if(pi == null){
            return null;
        }
        pi.updateKDP(plugin.data,isPro);
        plugin.data.savePlayerData(pi.uuid,pi.kill,pi.death,pi.getKDR(),pi.total_prize,pi.max_prize,pi.betted,pi.getScore());
        return pi;
    }


    void showHelp(CommandSender p){
        p.sendMessage("§e============== §d●§f●§a●§e　Man10 Fight Club　§d●§f●§a● §e===============");
        p.sendMessage("§e  by takatronix http://man10.red");
        p.sendMessage("§c* 赤いコマンドは管理者用です");
        p.sendMessage("§c/mfc entry - エントリ開始");
        p.sendMessage("§c/mfc cancel(close) - ゲームをキャンセルして返金->エントリへ");
        p.sendMessage("§c*/mfc stop - 停止");
        p.sendMessage("/mfc odds - Show Odds");
        p.sendMessage("/mfc bets - Show Bets");
        p.sendMessage("/mfc hide - MFCの表示をけす");
        p.sendMessage("/mfc show - MFCの表示を再開する");
        p.sendMessage("-----------エントリー中有効コマンド-----------");
        p.sendMessage("§c/mfc register [Fighter]      / Register fighter(s)");
        p.sendMessage("§c/mfc open [arena] [kit]- 投票開始");
        p.sendMessage("-----------オープン後有効コマンド-----------");
        p.sendMessage("/mfc bet [fighter] [money]   / Bet money on fighter");
        p.sendMessage("§c/mfc fight                 / Start Fight!!");
        p.sendMessage("-----------ホワイトリスト・ブラックリスト・プロコマンド-----------");
        p.sendMessage("§c/mfc whitelist list - ホワイリスト表示");
        p.sendMessage("§c/mfc whitelist add [username] - ホワイトリスト追加");
        p.sendMessage("§c/mfc whitelist delete [username] - ホワイトリスト削除");
        p.sendMessage("§c/mfc blacklist list - ブラックリスト表示");
        p.sendMessage("§c/mfc blacklist add [username] - ブラックリスト追加");
        p.sendMessage("§c/mfc blacklist delete [username] - ブラックリスト削除");
        p.sendMessage("§c/mfc prolist list - プロリスト表示");
        p.sendMessage("§c/mfc prolist add [username] - プロリスト追加");
        p.sendMessage("§c/mfc prolist delete [username] - プロリスト削除");

        p.sendMessage("§c/mfc reset [username] - ユーザー履歴を消去し、１度だけのチャンスを与える");
        p.sendMessage("-----------モード変更コマンド-----------");
        p.sendMessage("§c/mfc off - MFC停止");
        p.sendMessage("§c/mfc on - MFC開始(通常モード)");
        p.sendMessage("§c/mfc free - MFC開始(フリーモード)");
        p.sendMessage("§c/mfc whitelist - MFC開始(ホワイトリストモード)");
        p.sendMessage("§c/mfc pro - MFC開始(プロモード)");

        p.sendMessage("§c/mfc arena [KitName] - アリーナを固定");




        p.sendMessage("-----------履歴削除-----------");
        p.sendMessage("§c*/mfc reset [name] ユーザーデータ削除");
        p.sendMessage("§c*/mfc resethistory [name] ユーザー履歴削除");
        p.sendMessage("§c*/mfc resethistory.pro [name] ユーザー履歴削除");
        p.sendMessage("§c*/mfc update [name] ユーザーデータ更新");
        p.sendMessage("§c*/mfc update.pro [name] ユーザーデータ更新");
        p.sendMessage("§c*/mfc data [name] ユーザーデータ表示");
        p.sendMessage("§c*/mfc data.pro [name] ユーザーデータ表示");

        p.sendMessage("-----------アリーナ-----------");
        p.sendMessage("§c*/mfca create [アリーナ名]");
        p.sendMessage("§c*/mfca select [アリーナ名]");
        p.sendMessage("§c*/mfca delete [アリーナ名]");
        p.sendMessage("§c*/mfca list");
        p.sendMessage("§c*/mfca settp player1 - 選択中のPlayer1座標設定");
        p.sendMessage("§c*/mfca settp player2 - 選択中のPlayer2座標設定");
        p.sendMessage("§c*/mfca settp spawn - 選択中のPlayer1座標設定");
        p.sendMessage("----------------------");
        p.sendMessage("§c*/mfc autobet [money] - 自動ベットする金額");
        p.sendMessage("§c*/mfc fee [money] - register時に必要な金額");
        p.sendMessage("§c*/mfc prize [掛け率] - 賞金の比率");

        p.sendMessage("-----------キット-----------");
        p.sendMessage("§c*/mfckit help で詳細を確認");
        p.sendMessage("-----------戦績-----------");
        p.sendMessage("§c*/mfch MFC履歴");

    }
}
