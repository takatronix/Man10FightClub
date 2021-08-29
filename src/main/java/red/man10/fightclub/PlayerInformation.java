package red.man10.fightclub;
import org.bukkit.*;
import org.bukkit.entity.*;
import red.man10.Utility;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

//     プレイヤー情報
public class  PlayerInformation{
    UUID        uuid;
    String      name;
    boolean     isDead = false;
    //
    int         kill;
    int         death;
    double      total_prize;
    double      max_prize;
    double      betted;
    Date        datetime;

    public double getKDR() {
        if (this.death != 0) {
            return (double) this.kill / (double) this.death;
        }
        return 0;
    }
    public void updateKDP(FightClubData data,boolean isPro){
        kill = data.killCount(uuid,isPro);
        death = data.deathCount(uuid,isPro);
        total_prize = data.totalPrize(uuid,isPro);
        max_prize = data.maxPrize(uuid,isPro);
        betted = data.totalBetted(uuid,isPro);
        Bukkit.getLogger().log(Level.INFO,"updateKDR:"+name + ": "+getInfo());
    }

    Player getPlayer(){
        return Bukkit.getPlayer(uuid);
    }
    String getKDRString(){
        var kdrs = String.format("%.2f",getKDR());
        return kdrs;
    }
    /**
     * MFCScore
     * @return
     */
    int getScore(){
        double d = this.total_prize /  (double)(this.kill + this.death) * 0.001;
        return (int)d;
    }

    String getInfo(){
        String s = "§9§lK"+this.kill+"§f/§c§lD"+this.death+"§b§l("+getKDRString()+") §f§l総獲得賞金:"+ Utility.getPriceString(this.total_prize);
        return s;
    }

    String getDetail(){
        String s = "§9§lK"+this.kill+"§f/§c§lD"+this.death+"§b§l("+getKDRString()+") §f§l総獲得賞金:"+ Utility.getPriceString(this.total_prize) + " §f§l賭けられた金額"+Utility.getPriceString(this.betted)+ " §f§l最大獲得金額"+Utility.getPriceString(this.max_prize)  +" &f&lスコア:"+this.getScore();
        return s;
    }

}
