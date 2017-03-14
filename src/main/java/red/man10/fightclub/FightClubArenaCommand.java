package red.man10.fightclub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by takatronix on 2017/03/07.
 */
public class FightClubArenaCommand  implements CommandExecutor {
    private final FightClub plugin;

    public FightClubArenaCommand(FightClub plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0){
            CommandSender p = sender;
            p.sendMessage("§c/mfca setlobby - ロビーを設定する");
            p.sendMessage("§c/mfca create [アリーナ名](1)");
            p.sendMessage("§c/mfca select [アリーナ名]");
            p.sendMessage("§c/mfca delete [アリーナ名]");
            p.sendMessage("§c/mfca list");
            p.sendMessage("§c/mfca settp spawn - 選択中のアリーナ スポーン座標設定(2)");
            p.sendMessage("§c/mfca settp player1 - 選択中のアリーナ Player1座標設定(3)");
            p.sendMessage("§c/mfca settp player2 - 選択中のアリーナ Player2座標設定(4)");
            p.sendMessage("§cステージ作成する時->(1)(2)(3)(4) の順に実行");
            return false;
        }

        if(args[0].equalsIgnoreCase("create")){
            if(args.length != 2){
                return false;
            }
            plugin.createArena(sender,args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("select")){
            if(args.length != 2){
                return false;
            }
            plugin.selectArena(sender,args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("delete")){
            if(args.length != 2){
                return false;
            }
            plugin.deleteArena(sender,args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("list")){
            plugin.listArena(sender);
            return true;
        }
        if(args[0].equalsIgnoreCase("setlobby")){
            Player p = (Player)sender;
            plugin.setlobby(p);
            return true;
        }

        //
        if(args[0].equalsIgnoreCase("settp")) {

            plugin.settp((Player)sender,plugin.selectedArena,args[1]);
        }


        return true;
    }



}
