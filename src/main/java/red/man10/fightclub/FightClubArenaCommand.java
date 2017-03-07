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
            p.sendMessage("§c/mfca create [アリーナ名]");
            p.sendMessage("§c/mfca select [アリーナ名]");
            p.sendMessage("§c/mfca delete [アリーナ名]");
            p.sendMessage("§c/mfca list");
            p.sendMessage("§c/mfca settp player1 - 選択中のPlayer1座標設定");
            p.sendMessage("§c/mfca settp player2 - 選択中のPlayer2座標設定");
            p.sendMessage("§c/mfca settp spawn - 選択中のPlayer1座標設定");
            p.sendMessage("-----------アリーナ(Console)-----------");
            p.sendMessage("§c/mfca tpa - 登録者全員を選択中のアリーナ(spawn)へ移動");
            p.sendMessage("§c/mfca tpu [Player] player1");
            p.sendMessage("§c/mfca tpu [Player] player2");
            p.sendMessage("§c/mfca tpu [Player] spawn");
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
            return true;s
        }
        if(args[0].equalsIgnoreCase("list")){
            plugin.listArena(sender);
            return true;
        }

        //
        if(args[0].equalsIgnoreCase("settp")) {

            plugin.settp((Player)sender,plugin.selectedArena,args[1]);
        }
        //
        if(args[0].equalsIgnoreCase("tpa")) {

            plugin.tpa((Player)sender,plugin.selectedArena,args[1]);
        }


        return true;
    }



}
