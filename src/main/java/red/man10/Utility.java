package red.man10;

public class Utility {

    //　金額文字列作成
    public static String getPriceString(double price) {
        return String.format("§e§l%,d円§f", (long)price);
    }
}
