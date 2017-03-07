package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.SidebarDisplay;

import java.awt.*;

import static red.man10.fightclub.FightClub.Status.*;

/**
 * Created by takatronix on 2017/03/06.
 */
public class FightClubSideBar {
    SidebarDisplay sideBar = new SidebarDisplay();

    private final FightClub plugin;
    public FightClubSideBar(FightClub plugin) {
        this.plugin = plugin;
    }
    void show(){


      //  plugin.log("showing sidebar");
    sideBar.remove();
    sideBar = new SidebarDisplay();


    //sideBar.setScore("test",1);
    if(plugin.currentStatus == Closed){
       // plugin.log("showing sidebar close");

        showToAll();
        return;
    }if(plugin.currentStatus == Entry) {
         //   plugin.log("showing sidebar Entry");
            showWaiters();
            return;
        }

    if(plugin.currentStatus == Opened) {
       // plugin.log("showing sidebar closeOpen");
        showOdds();
        return;
    }

        if(plugin.currentStatus == Fighting) {
            showFighters();
            showToAll();
        }
    }


    void showOdds(){
        sideBar.setTitle("Man10 Fight Club ベット受付中!!-> /mfc");
        sideBar.setScore("§4残り時間",plugin.betTimer);

        double total = plugin.getTotalBets();

        for(int i = 0;i < plugin.fighters.size();i++){
            FightClub.FighterInformation f = plugin.fighters.get(i);

            String tx = String.format("%10s Odds:§bx§l%3.1f",f.name,plugin.getFighterOdds(f.uuid));
            sideBar.setScore(tx,plugin.getFighterBetCount(f.uuid));
        }

        sideBar.setScore("§d合計かけ金額：$"+total,0);


        sideBar.setScore("§a今回のゲームキット：" + plugin.selectedKit,0);

        sideBar.setScore("§e勝者への賞金：$"+plugin.getPrize(),0);
        sideBar.setScore("§b==========================",0);

        if(plugin.canStartGame()){
            sideBar.setScore("§eまもなく試合が開催されます！！！！",0);

        }else {
            sideBar.setScore("§eみなさんが、ベットしないと試合ははじまりません",0);

        }
        sideBar.setScore("§b/mfc§fで勝利者を予想しお金をかけよう！！",0);

        showToAll();
    }
    void showFighters(){
        sideBar.setTitle("§l Man10 Fight Club 対戦中!!!!");
        sideBar.setScore("§4残り時間",plugin.fightTimer);

        for(int i = 0;i < plugin.fighters.size();i++){
            FightClub.FighterInformation f = plugin.fighters.get(i);
            String s = "";


            if(i == 0){
                Double h = Bukkit.getPlayer(f.uuid).getHealth();
                String hl = String.format("%.1f",h);

                s =  "§c["+i+"]" + f.name + " Health:"+hl;
            }else{
                Double h = Bukkit.getPlayer(f.uuid).getHealth();
                String hl = String.format("%.1f",h);
                s =  "§9["+i+"]" + f.name + " Health:"+ hl;
            }
            sideBar.setScore(s,plugin.getFighterBetCount(f.uuid));
        }
        showToAll();
    }


    void showWaiters(){
        sideBar.setTitle("Man10 Fight Club 選手受付中 ");
        sideBar.setScore("残り時間",plugin.entryTimer);
        if(plugin.waiters.size() == 0){
            sideBar.setScore("/mfc register [name]で登録",0);
        }
        for(int i = 0;i < plugin.waiters.size();i++){
            FightClub.FighterInformation f = plugin.waiters.get(i);
            String s = "["+i+"]" + f.name ;
            sideBar.setScore(s,0);
           // Bukkit.getLogger().info("waiter" + s);
        }
        showToAll();
    }

    void showToAll(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            sideBar.setMainScoreboard(player);
            sideBar.setShowPlayer(player);

        }
    }
}
