package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static red.man10.fightclub.FightClub.Status.Closed;

/**
 * Created by takatronix on 2017/03/06.
 */
public class FightClubSideBar {
    SidebarDisplay sideBar = new SidebarDisplay();

    private final FightClub plugin;
    public FightClubSideBar(FightClub plugin) {
        this.plugin = plugin;
    }
    void show(Player p){

    sideBar.remove();
    sideBar = new SidebarDisplay();


    sideBar.setTitle("     Man10 Fight Club    ");
    //sideBar.setScore("test",1);
    if(plugin.currentStatus == Closed){

    }

    for(int i = 0;i < plugin.filghters.size();i++){
        FightClub.FighterInformation f = plugin.filghters.get(i);
        String s = "["+i+"]" + f.name + " Odds: x"+ plugin.getFighterOdds(f.uuid);
        sideBar.setScore(s,1);
    }



    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
        sideBar.setMainScoreboard(player);
        sideBar.setShowPlayer(player);

    }



}

}
