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

        if(args[0].equalsIgnoreCase("help")){
            showHelp(p);
            return true;
        }
        if(args[0].equalsIgnoreCase("clear")){
            if(!p.hasPermission(plugin.adminPermision)){
                p.sendMessage("管理者権限がありません");
                return false;
            }
            plugin.waiters.clear();
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
            plugin.prize = prize;
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
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" is already registered!");
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
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" is offline!!");
                return false;
            }

            boolean ret = plugin.unregisterFighter(fighter.getUniqueId());
            if (ret == false){
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" is already unregistered!");
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

            plugin.openGame();
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
                p.sendMessage(plugin.prefix + "戦闘中はベットできません");
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
            FightClub.PlayerInformation info = plugin.fighters.get(i);
            Player fighter = Bukkit.getPlayer(info.uuid);

            double price = plugin.getFighterBetMoney(info.uuid);
            int count = plugin.getFighterBetCount(info.uuid);

            double odds = plugin.getFighterOdds(info.uuid);

            p.sendMessage("["+i+"]:" +info.name +" Death:"+ info.isDead +"   $"+price +"  Count:"+count+"  倍率:"+odds+"倍");
        }
        p.sendMessage("-------------------------");
        p.sendMessage("total: $"+plugin.getTotalBet());

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
        p.sendMessage("§c*/mfc ban [User]");
        p.sendMessage("§c*/mfc unban [User]");
        p.sendMessage("§c*/mfc kick [User]");
        p.sendMessage("-----------エントリー中有効コマンド-----------");
        p.sendMessage("§c/mfc register [Fighter]      / Register fighter(s)");
        p.sendMessage("§c/mfc open - 投票開始");
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
        p.sendMessage("-----------モード変更コマンド-----------");
        p.sendMessage("§c/mfc off - MFC停止");
        p.sendMessage("§c/mfc on - MFC開始(通常モード)");
        p.sendMessage("§c/mfc free - MFC開始(フリーモード)");
        p.sendMessage("§c/mfc whitelist - MFC開始(ホワイトリストモード)");
        p.sendMessage("§c/mfc pro - MFC開始(プロモード)");


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

    }
}
