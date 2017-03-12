package red.man10;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by takatronix on 2017/03/04.
 */
public class VaultManager {
    private final JavaPlugin plugin;
    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public static Economy economy = null;

    private boolean setupEconomy() {
        plugin.getLogger().info("setupEconomy");
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin is not installed");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Can't get vault service");
            return false;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Economy setup");
        return economy != null;
    }

    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    public double  getBalance(UUID uuid){
        return economy.getBalance(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }

    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    public void showBalance(UUID uuid){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        double money = getBalance(uuid);
        p.getPlayer().sendMessage(ChatColor.YELLOW + "あなたの所持金は$" + (int)money);
    }
    /////////////////////////////////////
    //      引き出し
    /////////////////////////////////////
    public Boolean  withdraw(UUID uuid, double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if(p == null){
            Bukkit.getLogger().info(uuid.toString()+"は見つからない");
            return false;
        }
        EconomyResponse resp = economy.withdrawPlayer(p,money);
        if(resp.transactionSuccess()){
            if(p.isOnline()) {
                p.getPlayer().sendMessage(ChatColor.YELLOW + "$" + (int) money + "支払いました");
            }
            return true;
        }
        return  false;
    }
    /////////////////////////////////////
    //      お金を入れる
    /////////////////////////////////////
    public Boolean  deposit(UUID uuid,double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if(p == null){
            Bukkit.getLogger().info(uuid.toString()+"は見つからない");

            return false;
        }
        EconomyResponse resp = economy.depositPlayer(p,money);
        if(resp.transactionSuccess()){
            if(p.isOnline()){
                p.getPlayer().sendMessage(ChatColor.YELLOW + "$"+(int)money+"受取りました");
            }
            return true;
        }
        return  false;
    }

}
