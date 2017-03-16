package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.SidebarDisplay;

import java.awt.*;
import java.text.DecimalFormat;

import static red.man10.fightclub.FightClub.Status.*;

/**
 * Created by takatronix on 2017/03/06.
 */
public class FightClubSideBar {
    SidebarDisplay sideBar = new SidebarDisplay();
    public boolean hidden = false;

    private final FightClub plugin;
    public FightClubSideBar(FightClub plugin) {
        this.plugin = plugin;
    }
    void show(){


      //  plugin.log("showing sidebar");
    sideBar.remove();
    sideBar = new SidebarDisplay();

    if(hidden){
        return;
    }

    //sideBar.setScore("test",1);
    if(plugin.currentStatus == Closed){
       // plugin.log("showing sidebar close");
     //   showToAll();
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
           // showFighters();
           // showToAll();
        }
    }


    void showOdds(){
        sideBar.setTitle("Man10 Fight Club ベット受付中! §a/MFC");
        sideBar.setScore("§4残り時間",plugin.betTimer);

        double total = plugin.getTotalBet();

        for(int i = 0;i < plugin.fighters.size();i++){
            FightClub.PlayerInformation f = plugin.fighters.get(i);

            String tx = String.format("%10s Odds:§bx§l%3.3f",f.name,plugin.getFighterOdds(f.uuid));
            sideBar.setScore(tx,plugin.getFighterBetCount(f.uuid));

         //   setFighterInfo(f);

        }

        sideBar.setScore("§d合計かけ金額：$"+(int)total,0);


        sideBar.setScore("§a今回のアリーナ：" + plugin.selectedArena,0);
        sideBar.setScore("§a今回のキット：" + plugin.selectedKit,0);

        sideBar.setScore("§e§l勝者への賞金：$"+(int)plugin.getPrize(),0);
        sideBar.setScore("§b==========================",0);

       // sideBar.setScore("§e"+plugin.fighters.get(0).name+" Kill:"+plugin.kill0 + "/Death:"+plugin.death0+"/獲得$"+(int)plugin.prize0 ,0);
       // sideBar.setScore("§e"+plugin.fighters.get(1).name+" Kill:"+plugin.kill1 + "/Death:"+plugin.death1+"/獲得$"+(int)plugin.prize1 ,0);

        if(plugin.canStartGame()){
            sideBar.setScore("§eまもなく試合が開催されます！！！！",0);

        }else {
            sideBar.setScore("§eみなさんが、ベットしないと試合ははじまりません",0);

        }
        sideBar.setScore("§a/§lMFC§fで勝利者を予想しお金をかけよう！！",0);

        showToAll();
    }






    private static String[] suffix = new String[]{"","K", "M", "B", "T"};
    private static int MAX_LENGTH = 4;

    private static String money(double number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while(r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")){
            r = r.substring(0, r.length()-2) + r.substring(r.length() - 1);
        }
        return r;
    }



    void showWaiters(){
        sideBar.setTitle("Man10 Fight Club 選手受付中 §a/§lMFC");
        sideBar.setScore("獲得金額説明 K=1000/M=1000000",0);

       // String inf = "残り時間:"+plugin.entryTimer +" §e§l参加費:$"+(int)plugin.entryPrice;
       // sideBar.setScore(inf,0);

        if(plugin.waiters.size() == 0){
            sideBar.setScore("§a/§lMFC§f で登録",0);
        }
        for(int i = 0;i < plugin.waiters.size();i++){
            FightClub.PlayerInformation f = plugin.waiters.get(i);
/*
            String s= f.name + " §9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/§e§l$"+money(f.prize);
            if(s.length() > 40){
                s = s.substring(0,40);
            }

            sideBar.setScore(s,getScore(f));
*/
            setFighterInfo(f);
        }
         showToAll();
    }

    void setFighterInfo(FightClub.PlayerInformation f){

        String name = String.format("%-10s",f.name);
        String shortString = name.substring(0, Math.min(name.length(), 10));

        String s= shortString + " §9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/§e§l$"+money(f.prize);
        if(s.length() > 40){
            s = s.substring(0,40);
        }

        sideBar.setScore(s,plugin.getScore(f));
    }

    void addPlayer(Player p){

        sideBar.setMainScoreboard(p);
        sideBar.setShowPlayer(p);

    }

    void showToAll(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            sideBar.setMainScoreboard(player);
            sideBar.setShowPlayer(player);

        }
    }
}
