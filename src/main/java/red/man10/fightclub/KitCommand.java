package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by takatronix on 2017/03/06.
 */
public class KitCommand implements CommandExecutor {
    private FightClub plugin;
    final String pluginName = "Man10FightClub";

    final String permissionError = "§c§lコマンド権限がありません";

    // permissions
    final String helpPermission = "man10.red.mfckit.help";
    final String loadPermission = "man10.red.mfckit.load";
    final String savePermission = "man10.red.mfckit.save";
    final String setPermission = "man10.red.mfckit.set";
    final String listPermission = "man10.red.mfckit.list";
    final String deletePermission = "man10.red.mfckit.delete";
    final String pushPermission = "man10.red.mfckit.push";
    final String popPermission = "man10.red.mfckit.pop";

    boolean checkPermission(CommandSender sender,String permission){
        if(!sender.hasPermission(permission)){
            showErrorMessage(sender,permissionError);
            return true;
        }

        return false;
    }
    void showErrorMessage(CommandSender sender,String message){
        sender.sendMessage("§a"+message);
        Bukkit.getLogger().log(Level.WARNING,message);
    }
    void showMessage(CommandSender sender,String message){
        sender.sendMessage("§5"+message);
    }

    public KitCommand(FightClub plugin) {
        this.plugin = plugin;
        this.plugin.kitCommand = this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // 引数なし -> help
        if(args.length == 0){
            showHelp(sender);
            return false;
        }

        /////////////////////////////////////////
        //  Listコマンド
        if (args[0].equalsIgnoreCase("list")) {
            if(checkPermission(sender,listPermission))
                return false;

            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.list(sender);
            });
            return true;
        }

        /////////////////////////////////////////
        //  Deleteコマンド
        if (args[0].equalsIgnoreCase("delete")) {
            if(checkPermission(sender,deletePermission))
                return false;

            if (args.length != 2) {
                showErrorMessage(sender,"/mkit delete [キット名]");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.delete(sender,args[1]);
            });
            return true;
        }


        /////////////////////////////////////////
        //  Setコマンド
        if (args[0].equalsIgnoreCase("set")) {
            if(checkPermission(sender,setPermission))
                return false;

            Bukkit.getLogger().info("set");
            if (args.length != 3) {
                showErrorMessage(sender,"/mkit set [ユーザー名] [キット名]");
                return false;
            }

            Player t = Bukkit.getPlayer(args[1]);
            if(t.isOnline() == false){
                showErrorMessage(sender,t.getName() +"はオンラインではありません");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.load(t,args[2]);
            });
            return true;
        }

        // Pop
        if (args[0].equalsIgnoreCase("pop")) {
            if(checkPermission(sender,popPermission))
                return false;

            //    引数がある場合
            if (args.length == 2) {
                String name = args[1];
                Player target = Bukkit.getPlayer(name);
                if(target == null){
                    showErrorMessage(sender,name+"はオフラインです");
                    sender.sendMessage(name+"はオフラインです");
                    return false;
                }
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    this.pop(target);
                });
                return true;
            }

            if (sender instanceof Player){
                Player p = (Player) sender;
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    this.pop(p);
                });
                return true;
            }

            return true;
        }


        //  Pushユーザーデータを保存
        if (args[0].equalsIgnoreCase("push")) {
            if(checkPermission(sender,pushPermission))
                return false;

            //    引数がある場合(サーバor外部コマンド)
            if (args.length == 2) {
                String name = args[1];
                Player p = Bukkit.getPlayer(name);
                if(p == null){
                    showErrorMessage(sender,name+"はオフラインです");
                    return false;
                }

                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    this.push(p);
                });
                return true;
            }

            //      ユーザーコマンド
            if (sender instanceof Player){
                Player p = (Player) sender;
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    this.push(p);
                });

                return true;
            }
            return true;
        }



        //      以下はプレイヤー専用コマンド


        if (sender instanceof Player == false){
            showErrorMessage(sender,"プレイヤーのみのコマンドになります");
            return false;
        }
        Player player = (Player)sender;

        /////////////////////////////////////////
        //  Saveコマンド
        if (args[0].equalsIgnoreCase("save")) {
            if(checkPermission(sender,savePermission))
                return false;

            if (args.length != 2) {
                showErrorMessage(sender,"/mkit save [キット名]");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.save(player,args[1]);
            });
            return true;
        }
        /////////////////////////////////////////
        //  Loadコマンド
        if (args[0].equalsIgnoreCase("load")) {
            if(checkPermission(sender,loadPermission))
                return false;

            if (args.length != 2) {
                showErrorMessage(sender,"/mkit load [キット名]");
                return false;
            }
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                this.load(player,args[1]);
            });
            return true;
        }

        showHelp(sender);
        return true;
    }

    /**
     * MFC Kit Help
     * @param p
     */
    public void showHelp(CommandSender p){
        if(checkPermission(p,helpPermission))
            return ;
        p.sendMessage("§e==============§d●§f●§a●§e MFC Kit [§d●§f●§a●§e===============");
        p.sendMessage("" +
                "§e/mfckit list §f登録済みのキットを表示\n" +
                "§e/mfckit load [キット名] §fキットをロードする(いまのインベントリは消えます)\n" +
                "§e/mfckit save [キット名] §f現在のインベントリを保存する\n" +
                "§e/mfckit set [ユーザー名] [キット名] §fプレーヤーにキットを設定する\n" +
                "§e/mfckit delete [キット名] §f登録済みのキットを削除する\n" +
                "§e/mfckit push (プレイヤー名(なければ自分)) §f現在のインベントリを瞬間保存する\n" +
                "§e/mfckit pop (プレイヤー名(なければ自分)) §f瞬間保存したキットを復元する\n"
        );
    }



    //      キットを削除
    public boolean delete(CommandSender p, String kitName){
        String fileName = kitName;
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin("Man10FightClub").getDataFolder(), File.separator + "Kits");
        File f = new File(userdata, File.separator + fileName + ".yml");

        if(f.delete()){
            this.showMessage(p,kitName+"を削除した");
        }else{
            this.showErrorMessage(p,kitName+"削除に失敗した");
        }
        return false;
    }

    //      キットを保存する
    public boolean push(Player p){

        PlayerInventory inv= p.getInventory();
        String fileName = p.getUniqueId().toString();
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDataFolder(), File.separator + "Users");
        File f = new File(userdata, File.separator + fileName + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        f.delete();

        if (!f.exists()) {
            try {
                data.set("creator", p.getName());
                data.set("inventory",p.getInventory().getContents());
                data.set("armor",p.getInventory().getArmorContents());
                data.save(f);
                this.showMessage(p,"ユーザーキットをバックアップしました");
            } catch (IOException exception) {
                this.showErrorMessage(p,"キットpushに失敗した:"+exception.getMessage());
                exception.printStackTrace();
                return false;
            }
        }

        return true;

    }

    /**
     * キットを保存する
     * @param p
     * @param kitName
     * @return
     */
    public boolean save(Player p, String kitName){

        String fileName = kitName;
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDataFolder(), File.separator + "Kits");
        File f = new File(userdata, File.separator + fileName + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);
        if (!f.exists()) {
            try {
                data.set("creator", p.getName());
                data.set("inventory",p.getInventory().getContents());
                data.set("armor",p.getInventory().getArmorContents());
                data.save(f);
                this.showMessage(p,"キットを保存しました:"+kitName);
            } catch (IOException exception) {
                this.showErrorMessage(p,"キットの保存に失敗した"+exception.getMessage());
                exception.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * キットリストを表示する
     * @param p
     * @return
     */
    public boolean list(CommandSender p) {

        File folder = new File(Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDataFolder(), File.separator + "Kits");
        p.sendMessage("§e§l========== 登録済みのキット =========");
        int n = 1;
        File[] files = folder.listFiles();
        for (File f : files) {
            if (f.isFile()){
                String filename = f.getName();
                //      隠しファイルは無視
                if(filename.substring(0,1).equalsIgnoreCase(".")){
                    continue;
                }
                int point = filename.lastIndexOf(".");
                if (point != -1) {
                    filename =  filename.substring(0, point);
                }
                p.sendMessage( "§e§l"+n +": §f§l" + filename);
                n++;
            }
        }

        return true;
    }

    /**
     * キットを読み込む
     * @param p
     * @param kitName
     * @return
     */
    public boolean load(Player p, String kitName){

        String fileName = kitName;
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDataFolder(), File.separator + "Kits");
        File f = new File(userdata, File.separator + fileName + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);
        if (!f.exists()) {
            p.sendMessage("キットは存在しない:"+kitName);
            return false;
        }

        Object a = data.get("inventory");
        Object b = data.get("armor");

        if(a == null || b == null){
            this.showErrorMessage(p,"保存されたインベントリがない"+kitName);
            return true;
        }
        ItemStack[] inventory = null;
        ItemStack[] armor = null;
        if (a instanceof ItemStack[]){
            inventory = (ItemStack[]) a;
        } else if (a instanceof List){
            List lista = (List) a;
            inventory = (ItemStack[]) lista.toArray(new ItemStack[0]);
        }
        if (b instanceof ItemStack[]){
            armor = (ItemStack[]) b;
        } else if (b instanceof List){
            List listb = (List) b;
            armor = (ItemStack[]) listb.toArray(new ItemStack[0]);
        }
        p.getInventory().clear();
        p.getInventory().setContents(inventory);
        p.getInventory().setArmorContents(armor);

        this.showMessage(p,kitName+"キットを装備しました");
        return true;
    }

    /**
     * キットを復元する
     * @param p
     * @return
     */
    public boolean pop(Player p){

        String fileName = p.getUniqueId().toString();
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin("Man10Kit").getDataFolder(), File.separator + "Users");
        File f = new File(userdata, File.separator + fileName + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);
        if (!f.exists()) {
            this.showErrorMessage(p,"ユーザーに保存されたキットはありません");
            return false;
        }

        Object a = data.get("inventory");
        Object b = data.get("armor");

        if(a == null || b == null){
            this.showErrorMessage(p,"インベントリに読み込むものはありません");
            return true;
        }
        ItemStack[] inventory = null;
        ItemStack[] armor = null;
        if (a instanceof ItemStack[]){
            inventory = (ItemStack[]) a;
        } else if (a instanceof List){
            List lista = (List) a;
            inventory = (ItemStack[]) lista.toArray(new ItemStack[0]);
        }
        if (b instanceof ItemStack[]){
            armor = (ItemStack[]) b;
        } else if (b instanceof List){
            List listb = (List) b;
            armor = (ItemStack[]) listb.toArray(new ItemStack[0]);
        }
        p.getInventory().clear();
        p.getInventory().setContents(inventory);
        p.getInventory().setArmorContents(armor);

        this.showMessage(p,"ユーザーキットを復元しました");
        return true;
    }


}


