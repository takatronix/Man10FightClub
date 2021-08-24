package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.SidebarDisplay;
import red.man10.Utility;

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

    void hide(){
        hidden = true;
        show();
    }

    void show(){

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
            //sideBar.remove();
            //showFighters();
           // showToAll();
        }
    }


    void showOdds(){
        sideBar.setTitle(plugin.getModeText()+" ベット受付中! §a/MFC");
       // sideBar.setScore("§4残り時間",plugin.betTimer);

        double total = plugin.getTotalBet();

        for(int i = 0;i < plugin.fighters.size();i++){
            PlayerInformation f = plugin.fighters.get(i);

            var col = "§4§l";
            if(i == 1)
                col = "§9§l";
            String tx = String.format("%10s 倍率:§l%3.2f倍 KDR:%s",f.name,plugin.getFighterOdds(f.uuid),f.getKDRString());
            sideBar.setScore(col + tx,plugin.getFighterBetCount(f.uuid));
        }

        sideBar.setScore("§d合計かけ金額："+ Utility.getPriceString(total),0);


        sideBar.setScore("§a今回のステージ： §b§l" + plugin.selectedArena,0);
        sideBar.setScore("§a今回のキット： §b§l" + plugin.selectedKit,0);

        sideBar.setScore("§e§l勝者への賞金："+Utility.getPriceString(plugin.getPrize()),0);
        sideBar.setScore("§b==========================",0);

       // sideBar.setScore("§e"+plugin.fighters.get(0).name+" Kill:"+plugin.kill0 + "/Death:"+plugin.death0+"/獲得$"+(int)plugin.prize0 ,0);
       // sideBar.setScore("§e"+plugin.fighters.get(1).name+" Kill:"+plugin.kill1 + "/Death:"+plugin.ddeath1+"/獲得$"+(int)plugin.prize1 ,0);

        if(plugin.canStartGame()){
            sideBar.setScore("§eまもなく試合が始まります！",0);

        }else {
            sideBar.setScore("§eみなさんが、ベットしないと試合ははじまりません",0);

        }
        sideBar.setScore("§a/§lMFC§fで勝者を予想しお金をかけよう！！",0);

        showToAll();
    }

/*
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
*/



    void showWaiters(){

        sideBar.setTitle(plugin.getModeText() +" 選手受付中 §a/§lMFC");
       // sideBar.setScore("獲得金額説明 K=1,000/M=1,000,000",0);

       // String inf = "残り時間:"+plugin.entryTimer +" §e§l参加費:$"+(int)plugin.entryPrice;
       // sideBar.setScore(inf,0);

        if(plugin.waiters.size() == 0){
            sideBar.setScore("§a/§lMFC§f で登録",0);
        }
        for(int i = 0;i < plugin.waiters.size();i++){
            PlayerInformation f = plugin.waiters.get(i);
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

    void setFighterInfo(PlayerInformation f){

        String name = String.format("%-10s",f.name);
        String shortString = name.substring(0, Math.min(name.length(), 10));

        String s= shortString + " §9§lK"+f.kill+"§f/§c§lD"+f.death+"§f/§e§l"+Utility.getJpBal(f.total_prize)+"円";
        if(s.length() > 40){
            s = s.substring(0,40);
        }

        sideBar.setScore(s,f.getScore());
    }

    void addPlayer(Player p){
        // 興味がない人にはスコアボードを配信しない
        if(plugin.isUninterested(p)){
            return;
        }

        sideBar.setMainScoreboard(p);
        sideBar.setShowPlayer(p);
    }

    void showToAll(){
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            // 興味がない人にはスコアボードを配信しない
            if(plugin.isUninterested(player)){
                continue;
            }
            sideBar.setShowPlayer(player);
        }
    }
}
