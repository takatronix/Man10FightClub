package red.man10;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import red.man10.fightclub.FightClub;



public class LifeBar {

    private final FightClub plugin;

    public LifeBar(FightClub plugin) {
        this.plugin = plugin;
    }

    public String bname;
    public String rname;
    public String infoname;
    org.bukkit.boss.BossBar info = Bukkit.createBossBar(bname, BarColor.WHITE, BarStyle.SOLID, new BarFlag[0]);
    org.bukkit.boss.BossBar b = Bukkit.createBossBar(bname, BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
    org.bukkit.boss.BossBar r = Bukkit.createBossBar(rname, BarColor.RED, BarStyle.SOLID, new BarFlag[0]);

    public void setInfoName(String s){
        infoname = s;
        info.setTitle(infoname);
    }
    public String getInfoname(){
        return infoname;
    }
    public void setInfoBar(double d){
        info.setProgress(d);
    }
    public double getInfoBar(){
        return info.getProgress();
    }
    public void setInfoBarColor(BarColor c){
        info.setColor(c);
    }
    public BarColor getInfoBarColor(){
        return info.getColor();
    }
    public void addInfoPlayer(Player p){
        info.addPlayer(p);
    }
    public void removeInfoPlayer(Player p){
        info.removePlayer(p);
    }
    public void setInfoVisible(boolean b){
        if(b == true){
            info.setVisible(true);
        }
        if(b == false){
            info.setVisible(false);
        }
    }
    public void resetInfo(){
        info.setProgress(1);
        setInfoName("");
    }

//life
    public void addPlayer(Player p){
        r.addPlayer(p);
        b.addPlayer(p);
    }

    public void setVisible(Boolean bool){
        if(bool == true){
            b.setVisible(true);
            r.setVisible(true);
        }
        if(bool == false){
            b.setVisible(false);
            r.setVisible(false);
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
        info.removeAll();
    }
    public void clearInfoBar(){
        info.removeAll();
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
