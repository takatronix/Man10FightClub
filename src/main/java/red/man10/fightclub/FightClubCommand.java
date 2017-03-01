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
        if(args.length < 1){
            showHelp(p);
            return true;
        }

        if(args[0].equalsIgnoreCase("help")){
            showHelp(p);
            return true;
        }

        ////////////////////////////////////
        //          エントリー
        ////////////////////////////////////
        if(args[0].equalsIgnoreCase("entry")){
            if(args.length < 1) {
                p.sendMessage("/mfc entry [prize money]");
                return false;
            }

            if (plugin.currentStatus != FightClub.Status.Closed){
                p.sendMessage("You should cancel game!");
                return false;
            }

            plugin.currentStatus = FightClub.Status.Entry;

            p.sendMessage("Entry started");

            return false;
        }
        ////////////////////////////////////
        //        登録
        ////////////////////////////////////
        if(args[0].equalsIgnoreCase("register")){
            if(args.length != 2) {
                p.sendMessage("/mfc register [fighter]");
                return false;
            }
            Player fighter = Bukkit.getPlayer(args[1]);
            if (fighter == null) {
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" is offline!!");
                return false;
            }

            int ret = plugin.registerFighter(fighter.getUniqueId().toString(),args[1]);
            if (ret == -1){
                p.sendMessage(ChatColor.RED + "Error: " + args[1] +" is already registered!");
                return false;
            }
            showOdds(p);
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
        //////////////////////////////////
        ///       キャンセル
        //////////////////////////////////
        if(args[0].equalsIgnoreCase("cancel")){
            plugin.cancelGame(p);
            p.sendMessage("MFC Closed.");
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
            String buyer = p.getDisplayName();
            p.sendMessage(buyer);
            if(plugin.betFighter(fighter.getUniqueId().toString(),money,p.getUniqueId().toString(),buyer) == -1){
                p.sendMessage("Error :" + args[1] +"is not on entry!");
                return false;
            }

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
            String fighter =   plugin.filghters.get(info.fighterIndex).name;


            p.sendMessage("["+i+"]:" +info.buyerName +"   $"+price +" fighter:"+fighter);
        }
        p.sendMessage("-------------------------");
        p.sendMessage("total: $"+plugin.getTotalBets());

    }

    void showOdds(Player p){

        p.sendMessage("§e=========== §d●§f●§a●§e Man10 Fight Club Odds §d●§f●§a● §e===============");
        for(int i=0;i < plugin.filghters.size();i++){
            FightClub.FighterInformation info = plugin.filghters.get(i);
            Player fighter = Bukkit.getPlayer(info.UUID);

            double price = plugin.getFighterBetMoney(info.UUID);
            int count = plugin.getFighterBetCount(info.UUID);

            double odds = plugin.getFighterOdds(info.UUID);

            p.sendMessage("["+i+"]:" +info.name +"   $"+price +"  Count:"+count+"  Odds:"+odds);
        }
        p.sendMessage("-------------------------");
        p.sendMessage("total: $"+plugin.getTotalBets());

    }

    void showHelp(Player p){
        p.sendMessage("§e============== §d●§f●§a●§e　Man10 Fight Club　§d●§f●§a● §e===============");
        p.sendMessage("§e  by takatronix http://man10.red");
        p.sendMessage("§c* red commands for Admin");
        p.sendMessage("§c/mfc entry [prize money]     / Start entry");
        p.sendMessage("§c/mfc cancel                 / Cancel this game and pay money back");
        p.sendMessage("/mfc odds                   / Show Odds");
        p.sendMessage("/mfc bets                   / Show Bets");
        p.sendMessage("-----------エントリー中有効コマンド-----------");
        p.sendMessage("§c/mfc register [Fighter]      / Register fighter(s)");
        p.sendMessage("§c/mfc open                  / Game Open");
        p.sendMessage("-----------オープン後有効コマンド-----------");
        p.sendMessage("/mfc bet [fighter] [money]   / Bet money on fighter");
        p.sendMessage("§c/mfc start                 / Start Fight!!");
    }
}
