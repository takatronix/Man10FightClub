package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import sun.misc.UUDecoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by takatronix on 2017/04/02.
 */
public class FightClubList {
    String  name = "";
    Boolean enable = true;
    List<String> list = new ArrayList<String>();


    FightClubList(String name){
        load(name);
        this.name = name;
    }


    int find(String uuid){
        if(list.size() == -1){
            return -1;
        }

        for(int i = 0;i < list.size();i++){
            if(list.get(i).equals(uuid)){
                return i;
            }
        }
       return -1;
    }

    Boolean add(String uuid){
        if(find(uuid) != -1){
            return false;
        }
        list.add(uuid);
        save();
        return true;
    }
    Boolean delete(String uuid){
        int index = find(uuid);
        if(index == -1){
            return false;
        }
        list.remove(index);
        Bukkit.getLogger().info("保存した");
        save();
        return true;
    }

    Boolean load(String name){

        try{
            File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Man10FightClub").getDataFolder(), File.separator + name + ".yml");
            if(!f.exists()){
                return false;
            }
            FileConfiguration data = YamlConfiguration.loadConfiguration(f);
           list = data.getStringList("list");
        }catch (Exception e){
            return false;
        }

        return false;
    }




    Boolean save(){
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Man10FightClub").getDataFolder(), File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        try{
            data.set("title",name);
            data.set("list",list);
            data.save(f);
            Bukkit.getServer().broadcastMessage("list:保存した");

        } catch (Exception exp) {
            exp.printStackTrace();
            return false;
        }

        return true;
    }

    Boolean add(CommandSender s, Player p) {
        if(p == null){
            s.sendMessage("§2指定したユーザーはオフラインか存在しません");
            return false;
        }
        if(!add(p.getUniqueId().toString())){
            s.sendMessage("§2" + p.getName() + "は'" + name + "'にすでに登録されています。");
            return false;
        }
        s.sendMessage("§2" + p.getName() + "を'" + name + "'へ登録しました");
        return true;
    }
    Boolean delete(CommandSender s, Player p) {
        if(!delete(p.getUniqueId().toString())){
            s.sendMessage("§2" + p.getName() + "は'" + name + "'に登録されていません");
            return false;
        }
        s.sendMessage("§2" + p.getName() + "を'" + name + "'から削除しました");
        return true;
    }
    Boolean print(CommandSender s){

        s.sendMessage("リスト:"+name+"には、"+list.size()+"人登録されています");
        for(String id : list){
            UUID uuid = UUID.fromString(id);
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            s.sendMessage(p.getName());
        }
        return true;
    }
}
