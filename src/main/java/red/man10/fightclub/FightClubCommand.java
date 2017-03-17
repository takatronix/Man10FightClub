package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Filter;

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

        //      ON/OFFコマンド
        if(args[0].equalsIgnoreCase("on")) {
            plugin.enableMFC(sender,true);
            return false;
        }
        if(args[0].equalsIgnoreCase("off")) {
            plugin.enableMFC(sender,false);
            return false;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            plugin.reload(sender);
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
                p.sendMessage("You should cancel game!");
                return false;
            }

            plugin.currentStatus = FightClub.Status.Entry;

            p.sendMessage("Entry started");
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

            int ret = plugin.registerFighter(fighter.getUniqueId(),args[1]);
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

            int ret = plugin.unregisterFighter(fighter.getUniqueId());
            if (ret == -1){
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
            plugin.cancelGame();
            p.sendMessage("MFC Closed.");
            return true;
        }
        if(args[0].equalsIgnoreCase("close")){
            plugin.cancelGame();
            p.sendMessage("MFC Closed.");
            return true;
        }

        if(args[0].equalsIgnoreCase("lobby")){
            plugin.tpLobby(p);
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

            double money = Double.parseDouble(args[2]);
            Player fighter = Bukkit.getPlayer(args[1]);
            String buyer = p.getName();
            p.sendMessage(buyer);

            double balance = plugin.vault.getBalance(p.getUniqueId());
            p.sendMessage("あなたの残額は $"+balance +"です");
            if(balance < money){
                p.sendMessage(ChatColor.RED+ "残高が足りません！！");
                return false;
            }

            if(plugin.vault.withdraw(p.getUniqueId(),money) == false){
                p.sendMessage(ChatColor.RED+ "お金の引き出しに失敗しました" );
                return false;
            }

            if(plugin.betFighter(fighter.getUniqueId(),money,p.getUniqueId(),buyer) == -1){
                p.sendMessage("Error :" + args[1] +"is not on entry!");
                return false;
            }
            p.sendMessage(fighter.getName() +"へ、$" + money + "ベットしました！！");
            p.sendMessage(ChatColor.YELLOW + "あなたの残高は$" + plugin.vault.getBalance(p.getUniqueId()) +"です");
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
        p.sendMessage("invalid command");

        return false;
    }
    void showBets(Player p){

        p.sendMessage("§e=========== §d●§f●§a●§e Man10 Fight Club Buyer §d●§f●§a● §e===============");
        for(int i=0;i < plugin.bets.size();i++){
            FightClub.BetInformation info = plugin.bets.get(i);


           // Player fighter = Bukkit.getPlayer(info.UUID);

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

            p.sendMessage("["+i+"]:" +info.name +" Death:"+ info.isDead +"   $"+price +"  Count:"+count+"  Odds:"+odds);
        }
        p.sendMessage("-------------------------");
        p.sendMessage("total: $"+plugin.getTotalBet());

    }

    void showHelp(CommandSender p){
        p.sendMessage("§e============== §d●§f●§a●§e　Man10 Fight Club　§d●§f●§a● §e===============");
        p.sendMessage("§e  by takatronix http://man10.red");
        p.sendMessage("§c* red commands for Admin");
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
        p.sendMessage("-----------アリーナ-----------");
        p.sendMessage("§c*/mfca create [アリーナ名]");
        p.sendMessage("§c*/mfca select [アリーナ名]");
        p.sendMessage("§c*/mfca delete [アリーナ名]");
        p.sendMessage("§c*/mfca list");
        p.sendMessage("§c*/mfca settp player1 - 選択中のPlayer1座標設定");
        p.sendMessage("§c*/mfca settp player2 - 選択中のPlayer2座標設定");
        p.sendMessage("§c*/mfca settp spawn - 選択中のPlayer1座標設定");
       // p.sendMessage("-----------アリーナ(Console)-----------");
       // p.sendMessage("§c*/mfca tpa - 登録者全員を選択中のアリーナ(spawn)へ移動");
        //p.sendMessage("§c*/mfca tpu [Player] player1");
        //p.sendMessage("§c*/mfca tpu [Player] player2");
        //p.sendMessage("§c*/mfca tpu [Player] spawn");



    }
}
