package red.man10.fightclub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        showHelp(p);

        return true;
    }

    void showHelp(Player p){
        p.sendMessage("§e=========== <Man10 Fight Club> by takatronix http://man10.red===============");
        p.sendMessage("§c/mfc entry [prize money]     / Start entry");
        p.sendMessage("§c/mfc cancel                 / Cancel this game and pay money back");
        p.sendMessage("/mfc odds                   / Show Odds");
        p.sendMessage("-----------エントリー中有効コマンド-----------");
        p.sendMessage("§c/mfc register [Fighter]      / Register fighter(s)");
        p.sendMessage("§c/mfc open :ゲームオープン");
        p.sendMessage("-----------オープン後有効コマンド-----------");
        p.sendMessage("/mfc bet [fighter] [money]   / Bet money on fighter");
        p.sendMessage("§c/mfc start                 / Start Fight!!");
    }
}
