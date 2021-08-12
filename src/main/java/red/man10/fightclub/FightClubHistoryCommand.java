package red.man10.fightclub;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import red.man10.Utility;

/**
 * Created by takatronix on 2017/03/12.
 */
public class FightClubHistoryCommand  implements CommandExecutor {
    private final FightClub plugin;

    public FightClubHistoryCommand(FightClub plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0){
            CommandSender p = sender;
            p.sendMessage("§e/mfch prize        賞金ランキングを表示");
            p.sendMessage("§e/mfch kdr          強さランキングを表示");
            p.sendMessage("§e/mfch score        スコアランキングを表示");
            p.sendMessage("§e/mfch prize.pro    PRO)獲得賞金ランキングを表示");
            return false;
        }

        //  獲得賞金ランキング
        if(args[0].equalsIgnoreCase("prize")){
            ShowPrizeRanking(sender,false,0);
        }
        if(args[0].equalsIgnoreCase("prize.pro")){
            ShowPrizeRanking(sender,true,0);
        }
        //  KDR
        if(args[0].equalsIgnoreCase("kdr")){
            ShowKDRRanking(sender,false,0);
        }
        if(args[0].equalsIgnoreCase("kdr.pro")){
            ShowKDRRanking(sender,true,0);
        }
        //  Score
        if(args[0].equalsIgnoreCase("score")){
            ShowScoreRanking(sender,false,0);
        }
        if(args[0].equalsIgnoreCase("score.pro")){
            ShowScoreRanking(sender,true,0);
        }


        return true;
    }

    void ShowPrizeRanking(CommandSender sender,boolean isPro,int page){
        int rankPerPage = 10;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            var result = plugin.data.getPrizeRanking(isPro,rankPerPage,rankPerPage * page);

            if(isPro){
                sender.sendMessage("§6§l========= §c§lPro 合計獲得賞金ランキング  §6§l===========");
                sender.sendMessage("§6§lMFC Proで一番賞金をゲットしたプレイヤー");
            }else{
                sender.sendMessage("§e§l====== MFC 合計獲得賞金ランキング  ======");
                sender.sendMessage("§e§l一番賞金をゲットしたレイヤー");
            }
            int no = 1;
            for (var p:result) {
                sender.sendMessage("§7§l"+no+".§b§l"+p.name+"§7§l : "+Utility.getPriceString(p.total_prize));
                no++;
            }
        });
    }

    void ShowKDRRanking(CommandSender sender,boolean isPro,int page){

        int rankPerPage = 10;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            var result = plugin.data.getKDRRanking(isPro,rankPerPage,rankPerPage * page);

            if(isPro){
                sender.sendMessage("§6§l========= §c§lPro Kill/Death Rate ランキング  §6§l===========");
                sender.sendMessage("§6§lMFC Proで一番強いプレイヤー");
            }else{
                sender.sendMessage("§c§l====== MFC KDR ランキング  ======");
                sender.sendMessage("§6§lMFCで一番強いプレイヤー");
            }
            int no = 1;
            for (var p:result) {
                sender.sendMessage("§7§l"+no+".§c§l"+p.name+"§7§l : "+"§9§lK"+p.kill+"§f§l/§c§lD"+p.death + "§b§l KDR:"+p.getKDRString());
                no++;
            }
        });
    }

    void ShowScoreRanking(CommandSender sender,boolean isPro,int page){

        int rankPerPage = 10;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            var result = plugin.data.getScoreRanking(isPro,rankPerPage,rankPerPage * page);

            if(isPro){
                sender.sendMessage("§6§l========= §c§lPro MFCスコアランキング  §6§l===========");
                sender.sendMessage("§6§l人気ランキング)");
            }else{
                sender.sendMessage("§c§l====== MFCスコア ランキング  ======");
                sender.sendMessage("§6§l人気ランキング");
            }
            int no = 1;
            for (var p:result) {
                sender.sendMessage("§7§l"+no+".§c§l"+p.name+"§8§l : §d§l最高獲得金額:"+ Utility.getPriceString(p.max_prize) + " §b§lScore:"+p.getScore());
                no++;
            }
        });
    }

}
