package red.man10;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import red.man10.fightclub.FightClub;



public class LifeBar {

    private final FightClub plugin;

    public LifeBar(FightClub plugin) {
        this.plugin = plugin;
    }

    public String bname;
    public String rname;

    org.bukkit.boss.BossBar b = Bukkit.createBossBar(bname, BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
    org.bukkit.boss.BossBar r = Bukkit.createBossBar(rname, BarColor.RED, BarStyle.SOLID, new BarFlag[0]);



    public void addPlayer(Player p){
        r.addPlayer(p);
        b.addPlayer(p);
    }

    public void setVisible(Boolean bool){
        if(bool == true){
            r.setVisible(true);
            b.setVisible(true);
        }
        if(bool == false){
            r.setVisible(false);
            b.setVisible(false);
        }
    }

    public void setBname(String s){
        b.setTitle(s);
        return;
    }

    public void setRname(String s){
        r.setTitle(s);
        return;
    }

    public void setRBar(double d){
        r.setProgress(d);
    }

    public void setBBar(double d){
        b.setProgress(d);
    }

    public double getBBar(){
        return b.getProgress();
    }
    public double getRBar(){
        return r.getProgress();
    }
    public void clearBar(){
        b.removeAll();
        r.removeAll();
    }
    public void removeRPlayer(Player p){
        r.removePlayer(p);
    }
    public void removeBPlayer(Player p){
        b.removePlayer(p);
    }
    public void resetBar(){
        b.setProgress(1);
        r.setProgress(1);
        bname = null;
        rname = null;
        b.setTitle(bname);
        r.setTitle(rname);
    }

}
