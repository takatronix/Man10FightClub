package red.man10.fightclub;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

import static red.man10.fightclub.FightClub.Status.Closed;
import static red.man10.fightclub.FightClub.Status.Playing;

public final class FightClub extends JavaPlugin {

    //   状態遷移 これらの状態遷移する
    public enum Status {
        Closed,                 //  開催前
        Opened,                 //  募集中 予想の受付開始
        Playing,                //  対戦中
    }
    //      プレーヤ情報
    class  PlayerInformation{
        String UUID;       //  購入者のUUID
        String Name;       //  購入者の名前
    }
    //      賭け情報
    class  BetInformation{
        String buyerUUID;       //  購入者のUUID
        String buyerName;       //  購入者の名前
        int    playerIndex;     //  プレーヤ情報
        double bet;             //  掛け金
    }

    Status  currentStatus;

    //      対戦者リスト
    ArrayList<PlayerInformation> players = new ArrayList<PlayerInformation>();
    //      掛け金
    ArrayList<BetInformation> bets = new ArrayList<BetInformation>();

    //////////////////////////////////
    //    公開API
    //////////////////////////////////

    //      対戦者登録
    public int registerPlayer(String uuid){

        //      開催前でなければ登録できない
        if (currentStatus != Closed){
            return -1;
        }

        ////////////////////////////////////
        //      すでに登録されてたらエラー
        ////////////////////////////////////
        for(int i = 0;i < players.size();i++){
            PlayerInformation player = players.get(i);
            if(player.UUID == uuid){
                //  登録済みエラー表示
                return -1;
            }
        }
        //      追加
        PlayerInformation playerInfo = new PlayerInformation();
        players.add(playerInfo);

        return players.size();
    }
    //////////////////////////////////
    //    プレーヤにかけれた金額
    //////////////////////////////////
    public double getPlayerBets(int playerIndex){
        double totalBet = 0;
        for(int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            totalBet += bet.bet;
        }
        return totalBet;
    }

    ///////////////////////////////////
    //      トータル掛け金
    ///////////////////////////////////
    public double getTotalBets(){
        double totalBet = 0;
        for(int i = 0;i < players.size();i++){
            totalBet = getPlayerBets(i);
        }
        return totalBet;
    }
    //////////////////////////////////////////////
    //     プレイーやに賭ける 成功なら掛け金テーブルindex
    //////////////////////////////////////////////
    public int  betPlayer(int playerIndex,double price,String buyerUUID){

        //    buyerのお金がたらない　エラー


        BetInformation bet = new BetInformation();
        bet.bet = price;
        bet.playerIndex = playerIndex;
        bet.buyerUUID = buyerUUID;
        bets.add(bet);

        return bets.size();
    }
    //////////////////////////////////////////////
    //      ゲームを中断する  払い戻し後ステータスを Closedへ
    //////////////////////////////////////////////
    public int cancelGame(){

        //   払い戻し処理


        bets.clear();
        players.clear();
        currentStatus = Closed;
        return 0;
    }

    //      ゲーム開始
    public int startGame(){
        currentStatus = Playing;
    }

    //      対戦終了　winPlayer = -1 終了
    public int endGame(int winPlayer){
        if (winPlayer == -1){
            return cancelGame();
        }

        //  掛け金の計算
        double total  = getTotalBets();
        double winBet = getPlayerBets(winPlayer);
        double tax = 0;

        //    オッズとは
        //  （賭けられたお金の合計 － 手数料）÷【賭けに勝つ人達の勝ちに賭けた総合計金額】
        double odds = (total - tax) / winBet;

        for (int i = 0;i < bets.size();i++){
            BetInformation bet = bets.get(i);
            if (bet.playerIndex != winPlayer){
                continue;
            }
            //      プレイヤーへの支払い金額
            double playerPayout = bet.bet * odds;
            //      プレイヤーへ支払い

            //      通知
        }

        //      終了
        bets.clear();
        players.clear();
        currentStatus = Closed;
        return 0;
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
