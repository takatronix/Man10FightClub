package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by takatronix on 2017/03/07.
 */
public class FightClubArenaCommand  implements CommandExecutor {
    private final FightClub plugin;
    ArrayList<String> defaultArenas = new ArrayList<String>();


    public FightClubArenaCommand(FightClub plugin) {
        this.plugin = plugin;
    }
    String adminPermission = "man10.fightclub.admin";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!sender.hasPermission(adminPermission)){
            sender.sendMessage("§c管理者権限が必要です");
            return false;
        }

        if(args.length == 0){
            CommandSender p = sender;
            p.sendMessage("§c/mfca setlobby - ロビーを設定する");
            p.sendMessage("§c/mfca create [アリーナ名](1)");
            p.sendMessage("§c/mfca select [アリーナ名]");
            p.sendMessage("§c/mfca delete [アリーナ名]");
            p.sendMessage("§c/mfca list");
            p.sendMessage("§c/mfca settp spawn - 選択中のアリーナ スポーン座標設定(2)");
            p.sendMessage("§c/mfca settp player1 - 選択中のアリーナ Player1座標設定(3)");
            p.sendMessage("§c/mfca settp player2 - 選択中のアリーナ Player2座標設定(4)");
            p.sendMessage("§c ----Arena毎のkitを設定");
            p.sendMessage("§c/mfca kit list - 選択中のアリーナのキットリスト(設定がなければデフォルト全部)");
            p.sendMessage("§c/mfca kit add [kitname] 選択中のアリーナにキットを登録する");
            p.sendMessage("§c/mfca kit delete [kitname] 選択中のアリーナからキットを削除する");
            p.sendMessage("§c/mfca kit reset - 選選択中のアリーナの選択キットをすべて削除");

            p.sendMessage("§cステージ作成する時->(1)(2)(3)(4) の順に実行");
            return false;
        }

        // MFCA Kitコマンド
        if(args[0].equalsIgnoreCase("kit")){
            if(plugin.selectedArena == null){
                sender.sendMessage("Arenaが選択されていません");
                return false;
            }
            if(args.length < 2){
                sender.sendMessage("/mfca kit [list/add/delete/reset]");
                return false;
            }
            //  選択中のアリーナを表示
            if(args[1].equalsIgnoreCase("list")){
                this.listArenaKits(sender, plugin.selectedArena);
                return true;
            }
            // 選択中のアリーナに追加
            if(args[1].equalsIgnoreCase("add")){
                this.addArenaKit(sender, plugin.selectedArena,args[2]);
                return true;
            }
            // 削除
            if(args[1].equalsIgnoreCase("delete")){
                this.deleteArenaKit(sender, plugin.selectedArena,args[2]);
                return true;
            }
            // リセット
            if(args[1].equalsIgnoreCase("reset")){
                this.resetArenaKit(sender, plugin.selectedArena);
                return true;
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("create")){
            if(args.length != 2){
                return false;
            }
            this.createArena(sender,args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("select")){
            if(args.length != 2){
                return false;
            }
            this.selectArena(sender,args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("delete")){
            if(args.length != 2){
                return false;
            }
            this.deleteArena(sender,args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("list")){
            this.listArena(sender);
            return true;
        }
        if(args[0].equalsIgnoreCase("setlobby")){
            Player p = (Player)sender;
            plugin.setlobby(p);
            return true;
        }
        if(args[0].equalsIgnoreCase("settp")) {

            plugin.settp((Player)sender,plugin.selectedArena,args[1]);
        }
        return true;
    }
    private void log(String text){
        Bukkit.getLogger().info("[MFCA]:"+text);
    }


    /**
     * アリーナ設定をconfigから読み込む
     */
    void loadArenaConfig(){
        log("Arenaリストをよみこみちう");
        plugin.selectedArena = plugin.getConfig().getString("selectedArena");
        Object o =  plugin.getConfig().get("Arenas");
        if(o != null){
            this.defaultArenas = (ArrayList<String>)o;
            log("Arenaリストをよんだ");
        }
    }

    /**
     * アリーナの作成
     * @param p
     * @param arena
     * @return
     */
    public boolean createArena(CommandSender p,String arena){
        //  すでにアリーナが存在する
        if(defaultArenas.contains(arena)){
            p.sendMessage("その名前のアリーナはすでに登録済みです");
            return false;
        }
        defaultArenas.add(arena);
        plugin.getConfig().set("Arenas",defaultArenas);
        plugin.saveConfig();;
        p.sendMessage(arena+" を登録しました");
        selectArena(p,arena);
        return true;
    }

    /**
     * アリーナの削除
     * @param p
     * @param arena
     * @return
     */
    public int deleteArena(CommandSender p,String arena){
        for(int i=0;i<defaultArenas.size();i++){
            if(defaultArenas.get(i).equalsIgnoreCase(arena)){
                defaultArenas.remove(i);
                p.sendMessage(arena+" は削除されました");
                plugin.getConfig().set("Arenas",defaultArenas);
                plugin.saveConfig();
                return i;
            }
        }
        p.sendMessage(arena+"は存在しません");
        return -1;
    }

    /**
     * アリーナの選択
     * @param p
     * @param arena
     * @return
     */
    public int selectArena(CommandSender p,String arena){
        for(int i=0;i<defaultArenas.size();i++){
            if(defaultArenas.get(i).equalsIgnoreCase(arena)){
                plugin.selectedArena = arena;
                plugin.getConfig().set("selectedArena",plugin.selectedArena);
                plugin.saveConfig();
                p.sendMessage(arena+" selected");
                plugin.tp((Player)p,plugin.selectedArena,"spawn");
                return i;
            }
        }
        p.sendMessage(arena+" は見つからない");
        return -1;
    }

    /**
     * アリーナの一覧
     * @param p
     * @return
     */
    public int listArena(CommandSender p) {
        p.sendMessage("------arena list-----");
        for (String arena : defaultArenas) {
            if (arena.equalsIgnoreCase(plugin.selectedArena)) {
                p.sendMessage(arena + ":(選択中)");
            } else {
                p.sendMessage(arena);
            }
        }
        return defaultArenas.size();
    }

    // Arena Kits

    /**
     * Arena Kitリストを取得
     * @param arena アリーナ名
     * @return リスト
     */
    public ArrayList<String> getArenaKits(String arena){
        log("ArenaKitリストをよみこみちう:"+arena);
        Object o =  plugin.getConfig().get("ArenaKit."+arena);
        if(o != null){
            var result = (ArrayList<String>)o;
            log("ArenaKitリストを読み込んだ");
            return result;
        }
        log("ArenaKitリストがないので空のリストを作成");
        return new ArrayList<String>();
    }

    /**
     * 指定したアリーナは存在する？
     * @param arena
     * @return
     */
    boolean existArena(String arena){
        if(this.defaultArenas.contains(arena))
            return true;
        return false;
    }
    /**
     * アリーナのKitリストを表示
     * @param p
     * @param arena
     * @return
     */
    public int listArenaKits(CommandSender p,String arena) {
        p.sendMessage("§5§l"+arena + "のキットリスト----");
        var kitList = getArenaKits(arena);
        for(var kit : getArenaKits(arena)){
            p.sendMessage(kit);
        }
        return kitList.size();
    }

    /**
     * アリーナKitに追加
     * @param p
     * @param arena
     * @param kitName
     * @return
     */
    public boolean addArenaKit(CommandSender p,String arena,String kitName) {

        //  アリーナ存在チェック
        if (!existArena(arena)) {
            p.sendMessage("そのアリーナは存在しない:" + arena);
            return false;
        }
        // デフォルトのキットに存在するか？その名前
        var defaultKits = plugin.kitCommand.getList();
        if (!defaultKits.contains(kitName)) {
            p.sendMessage("デフォルトキットに" + kitName + "は存在しないため登録しない");
            return false;
        }
        // アリーナキットを読み込む
        var arenaKits = getArenaKits(arena);
        if (arenaKits.contains(kitName)) {
            p.sendMessage("そのキットはすでにこのアリーナには登録されています");
            return false;
        }
        arenaKits.add(kitName);

        //  ArenaKit.[ArenaName] にキットを保存
        plugin.getConfig().set("ArenaKit." + arena, arenaKits);
        plugin.saveConfig();
        p.sendMessage(arena+"に"+kitName+"を追加しました");
        return true;
    }

    /**
     * アリーナキットを削除
     * @param p
     * @param arena
     * @param kitName
     * @return
     */
    public boolean deleteArenaKit(CommandSender p,String arena,String kitName) {

        //  アリーナ存在チェック
        if (!existArena(arena)) {
            p.sendMessage("そのアリーナは存在しない:" + arena);
            return false;
        }
        // アリーナキットを読み込む
        var arenaKits = getArenaKits(arena);
        if (!arenaKits.contains(kitName)) {
            p.sendMessage("そのキット名の登録はない");
            return false;
        }
        // 削除
        arenaKits.remove(kitName);

        //  ArenaKit.[ArenaName] にキットを保存
        plugin.getConfig().set("ArenaKit." + arena, arenaKits);
        plugin.saveConfig();
        p.sendMessage(arena+"から"+kitName+"を削除しました");
        return true;
    }

    /**
     * アリーナのキットをリセット
     * @param p
     * @param arena
     * @return
     */
    public boolean resetArenaKit(CommandSender p,String arena) {

        //  アリーナ存在チェック
        if (!existArena(arena)) {
            p.sendMessage("そのアリーナは存在しない:" + arena);
            return false;
        }

        // アリーナキットを読み込む
        var arenaKits = getArenaKits(arena);

        // すべて削除
        arenaKits.clear();

        //  ArenaKit.[ArenaName] にキットを保存
        plugin.getConfig().set("ArenaKit." + arena, arenaKits);
        plugin.saveConfig();
        p.sendMessage(arena+"のキット全削除しました");
        return true;
    }

}
