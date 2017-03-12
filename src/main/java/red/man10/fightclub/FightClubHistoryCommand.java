package red.man10.fightclub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by takatronix on 2017/03/12.
 */
public class FightClubHistoryCommand  implements CommandExecutor {
    private final FightClub plugin;

    public FightClubHistoryCommand(FightClub plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {



        return true;
    }
}
