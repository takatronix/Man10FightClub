package red.man10.fightclub;

import org.bukkit.plugin.java.JavaPlugin;

public final class FightClub extends JavaPlugin {

    //   状態遷移
    public enum Status {
        Closed,                 //  開催前
        Opened,                 //  募集中 予想の受付開始
        Playing,                //  対戦中
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
