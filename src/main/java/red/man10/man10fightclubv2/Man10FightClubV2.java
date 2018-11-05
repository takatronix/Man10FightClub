package red.man10.man10fightclubv2;

import org.bukkit.plugin.java.JavaPlugin;

public final class Man10FightClubV2 extends JavaPlugin {

    MySQLManager mysql;

    @Override
    public void onEnable() {
        // Plugin startup logic
        mysql = new MySQLManager(this,"Man10FightClubV2");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
