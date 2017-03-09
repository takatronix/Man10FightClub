package red.man10;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import red.man10.fightclub.FightClub;

/**
 * Created by sho-pc on 2017/03/10.
 */
public class TitleBar {
    private final FightClub plugin;
    public TitleBar(FightClub plugin) {
        this.plugin = plugin;
    }
    public void sendTitle(Player p,String mainText,String subText,int fadeIn,int stay,int fadeOut){
        if(mainText == null){
            p.sendTitle("",subText,fadeIn,stay,fadeOut);
        }else{
            p.sendTitle(mainText,subText,fadeIn,stay,fadeOut);
        }
    }
    public void sendTitleToAll(String mainText,String subText,int fadeIn,int stay,int fadeOut){
        if(mainText == null){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.sendTitle("",subText,fadeIn,stay,fadeOut);
            }
        }else{
            for(Player p : Bukkit.getOnlinePlayers()){
                p.sendTitle(mainText,subText,fadeIn,stay,fadeOut);
            }
        }
    }
    public void sendTitleWithSound(Player p,String mainText, String subText, int fadeIn, int stay, int fadeOut,Sound s, float volume,float pitch){
        if(mainText == null){
            p.playSound(p.getLocation(),s,volume,pitch);
            p.sendTitle("",subText,fadeIn,stay,fadeOut);
        }else{
            p.playSound(p.getLocation(),s,volume,pitch);
            p.sendTitle(mainText,subText,fadeIn,stay,fadeOut);
        }
    }
    public void sendTitleToAllWithSound(String mainText,String subText,int fadeIn,int stay,int fadeOut,Sound s,float volume,float pitch){
        if(mainText == null){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.playSound(p.getLocation(),s,volume,pitch);
                p.sendTitle("",subText,fadeIn,stay,fadeOut);
            }
        }else{
            for(Player p : Bukkit.getOnlinePlayers()){
                p.playSound(p.getLocation(),s,volume,pitch);
                p.sendTitle(mainText,subText,fadeIn,stay,fadeOut);
            }
        }
    }

}
