package red.man10.fightclub;

import org.bukkit.ChatColor;
import red.man10.SkullMaker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by takatronix on 2017/03/06.
 */
public class FightClubGUI {
    private final FightClub plugin;

    public FightClubGUI(FightClub plugin) {
        this.plugin = plugin;
    }

    public void clickItem(InventoryClickEvent e) {


        //try {
        if (e.getClickedInventory() != null) {
            if (e.getInventory().getName().equalsIgnoreCase("§c§l         ベットメニュー")) {
                Player p = (Player) e.getWhoClicked();
                FightClub.FighterInformation info = plugin.filghters.get(0);
                FightClub.FighterInformation info1 = plugin.filghters.get(1);
                if (e.getCurrentItem().getType() == Material.SKULL_ITEM) {
                    priceMenu(p, e.getCurrentItem().getItemMeta().getDisplayName());
                    e.setCancelled(true);
                } else {
                    e.setCancelled(true);
                }

            } else if (e.getInventory().getItem(52).getItemMeta().getDisplayName().equalsIgnoreCase("§c§lキャンセル")) {
                Player p = (Player) e.getWhoClicked();
                String val = e.getClickedInventory().getItem(50).getItemMeta().getLore().get(1);
                if (val.length() <= 8) {
                    if (e.getSlot() == 46) { //0
                        if(e.getInventory().getItem(50).getItemMeta().getLore().get(1).length() > 0){
                            moveD(e.getClickedInventory());
                            createDisplay(e.getClickedInventory(), p, 0);
                        }
                    } else if (e.getSlot() == 37) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 1);
                    } else if (e.getSlot() == 38) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 2);
                    } else if (e.getSlot() == 39) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 3);
                    } else if (e.getSlot() == 28) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 4);
                    } else if (e.getSlot() == 29) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 5);
                    } else if (e.getSlot() == 30) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 6);
                    } else if (e.getSlot() == 19) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 7);
                    } else if (e.getSlot() == 20) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 8);
                    } else if (e.getSlot() == 21) {
                        moveD(e.getClickedInventory());
                        createDisplay(e.getClickedInventory(), p, 9);
                    } else if (e.getSlot() == 48) {
                        clearCalc(e.getClickedInventory());
                    } else if (e.getSlot() == 52) {
                        p.closeInventory();
                    } else if (e.getSlot() == 50) {
                        placeBetGUI(e.getInventory(),p);
                        //確認処理
                    }
                    e.setCancelled(true);
                }else{
                    if(e.getSlot() == 48){
                        clearCalc(e.getInventory());
                    }else if(e.getSlot() == 50){
                        placeBetGUI(e.getInventory(),p);

                    }else if(e.getSlot() == 52){
                        p.closeInventory();
                    }else{
                        e.setCancelled(true);
                        p.sendMessage("上限！！！");
                    }
                    e.setCancelled(true);
                }
            }
        }else{
            //その他のインベントリ
        }
        //}catch (Exception ee){
    }
    void placeBetGUI(Inventory i, Player p){
        int money = Integer.parseInt(i.getItem(50).getItemMeta().getLore().get(1)); //設定したbal

        String fighterName = i.getItem(33).getItemMeta().getDisplayName();
        Player fighterPlayer = Bukkit.getPlayer(fighterName);

        UUID fighter = fighterPlayer.getUniqueId(); //    ShoへここへターゲットのUUID


        String buyer = p.getName();
        p.sendMessage(buyer);

        double balance = plugin.vault.getBalance(p.getUniqueId());
        p.sendMessage("あなたの残額は $"+balance +"です");
        if(balance < money){
            p.sendMessage(ChatColor.RED+ "残高が足りません！！");
            return;
        }

        if(plugin.vault.withdraw(p.getUniqueId(),money) == false){
            p.sendMessage(ChatColor.RED+ "お金の引き出しに失敗しました" );
            return;
        }

        if(plugin.betFighter(fighter,money,p.getUniqueId(),buyer) == -1){
            p.sendMessage("ベットできませんでした");
            p.closeInventory();
            return;
        }
        p.sendMessage(fighterName +"へ、$" + money + "ベットしました！！");
        p.sendMessage(ChatColor.YELLOW + "あなたの残高は$" + plugin.vault.getBalance(p.getUniqueId()) +"です");
        //  plugin.showSideBar(p);
        plugin.updateSidebar();;
        p.closeInventory();
        return;
    }

    void clearCalc(Inventory e){
        e.setItem(0, new ItemStack(Material.AIR));
        e.setItem(1, new ItemStack(Material.AIR));
        e.setItem(2, new ItemStack(Material.AIR));
        e.setItem(3, new ItemStack(Material.AIR));
        e.setItem(4, new ItemStack(Material.AIR));
        e.setItem(5, new ItemStack(Material.AIR));
        e.setItem(6, new ItemStack(Material.AIR));
        e.setItem(7, new ItemStack(Material.AIR));
        e.setItem(8, new ItemStack(Material.AIR));



        String betp = e.getItem(33).getItemMeta().getDisplayName();
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemMeta ac = Accept.getItemMeta();
        String val = e.getItem(50).getItemMeta().getLore().get(1);

        ac.setDisplayName("§a§l確認");
        ArrayList<String> conf = new ArrayList<String>();
        conf.add("§e§l" + betp + "に");
        conf.add("");
        conf.add("§e§l賭ける");
        ac.setLore(conf);
        Accept.setItemMeta(ac);

        e.setItem(50, Accept);


    }


    void createDisplay(Inventory i,Player p,int num){
        int l = i.getItem(50).getItemMeta().getLore().get(2).length();
        ItemStack item = new ItemStack(Material.AIR);
        if(num==0){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build();
            setTextPrice(i, 0);
        }else if(num==1){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build();
            setTextPrice(i, 1);
        }else if(num==2){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build();
            setTextPrice(i, 2);
        }else if(num==3){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build();
            setTextPrice(i, 3);
        }else if(num==4){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build();
            setTextPrice(i, 4);
        }else if(num==5){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build();
            setTextPrice(i, 5);
        }else if(num==6){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build();
            setTextPrice(i, 6);
        }else if(num==7){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build();
            setTextPrice(i, 7);
        }else if(num==8){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build();
            setTextPrice(i, 8);
        }else if(num==9){
            item = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build();
            setTextPrice(i, 9);
        }

        i.setItem(8, item);
    }

    void setTextPrice(Inventory i,int num){
        String betp = i.getItem(33).getItemMeta().getDisplayName();
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemMeta ac = Accept.getItemMeta();
        String val = i.getItem(50).getItemMeta().getLore().get(1);

        ac.setDisplayName("§a§l確認");
        ArrayList<String> conf = new ArrayList<String>();
        conf.add("§e§l" + betp + "に");
        conf.add(val + num);
        conf.add("§e§l賭ける");
        ac.setLore(conf);
        Accept.setItemMeta(ac);

        i.setItem(50, Accept);

    }

    void moveD(Inventory i){
        i.setItem(0, i.getItem(1));
        i.setItem(1, i.getItem(2));
        i.setItem(2, i.getItem(3));
        i.setItem(3, i.getItem(4));
        i.setItem(4, i.getItem(5));
        i.setItem(5, i.getItem(6));
        i.setItem(6, i.getItem(7));
        i.setItem(7, i.getItem(8));
    }

    public void priceMenu(Player p,String betp) {
        //ItemStack head = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/7c57f9192e81eb6897c24ecd4935cfb5a731a6f9a57abb51f2b35e8b4be7ebc").build();
        Inventory inv = Bukkit.createInventory(null, 54, betp + "にベットする");
        ItemStack i0 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build();
        ItemStack i1 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build();
        ItemStack i2 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build();
        ItemStack i3 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build();
        ItemStack i4 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build();
        ItemStack i5 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build();
        ItemStack i6 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build();
        ItemStack i7 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build();
        ItemStack i8 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build();
        ItemStack i9 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build();

        ItemStack price = new ItemStack(Material.EMERALD, 1);
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(betp);
        meta.setOwner(betp);
        skull.setItemMeta(meta);

        ArrayList<String> conf = new ArrayList<String>();
        conf.add("§e§l" + betp + "に");
        conf.add("");
        conf.add("§e§l賭ける");

        ItemStack clear = new ItemStack(Material.TNT, 1);
        ItemMeta clearm = clear.getItemMeta();
        clearm.setDisplayName("§c§lクリア");
        clear.setItemMeta(clearm);


        ArrayList<String> a = new ArrayList<String>();


        ItemMeta am = Accept.getItemMeta();
        ItemMeta cm = cancel.getItemMeta();

        a.add("§d§l掛け金");
        am.setLore(conf);


        am.setDisplayName("§a§l確認");
        cm.setDisplayName("§c§lキャンセル");

        Accept.setItemMeta(am);
        cancel.setItemMeta(cm);


        ItemMeta i0m = i0.getItemMeta();
        ItemMeta i1m = i1.getItemMeta();
        ItemMeta i2m = i2.getItemMeta();
        ItemMeta i3m = i3.getItemMeta();
        ItemMeta i4m = i4.getItemMeta();
        ItemMeta i5m = i5.getItemMeta();
        ItemMeta i6m = i6.getItemMeta();
        ItemMeta i7m = i7.getItemMeta();
        ItemMeta i8m = i8.getItemMeta();
        ItemMeta i9m = i9.getItemMeta();

        i0m.setDisplayName("§7§l0");
        i1m.setDisplayName("§7§l1");
        i2m.setDisplayName("§7§l2");
        i3m.setDisplayName("§7§l3");
        i4m.setDisplayName("§7§l4");
        i5m.setDisplayName("§7§l5");
        i6m.setDisplayName("§7§l6");
        i7m.setDisplayName("§7§l7");
        i8m.setDisplayName("§7§l8");
        i9m.setDisplayName("§7§l9");

        i0.setItemMeta(i0m);
        i1.setItemMeta(i1m);
        i2.setItemMeta(i2m);
        i3.setItemMeta(i3m);
        i4.setItemMeta(i4m);
        i5.setItemMeta(i5m);
        i6.setItemMeta(i6m);
        i7.setItemMeta(i7m);
        i8.setItemMeta(i8m);
        i9.setItemMeta(i9m);

        inv.setItem(19, i7);
        inv.setItem(20, i8);
        inv.setItem(21, i9);

        inv.setItem(28, i4);
        inv.setItem(29, i5);
        inv.setItem(30, i6);

        inv.setItem(37, i1);
        inv.setItem(38, i2);
        inv.setItem(39, i3);

        inv.setItem(46, i0);

        inv.setItem(50, Accept);
        inv.setItem(52, cancel);

        inv.setItem(33, skull);

        inv.setItem(48, clear);

        p.openInventory(inv);
    }

    void betMenu(Player p){
        Inventory bet = Bukkit.createInventory(null, 27, "§c§l         ベットメニュー");

        FightClub.FighterInformation info = plugin.filghters.get(0);
        FightClub.FighterInformation info1 = plugin.filghters.get(1);

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(info.name);
        meta.setOwner(info.name);
        skull.setItemMeta(meta);

        ItemStack skull1 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta1 = (SkullMeta) skull1.getItemMeta();
        meta1.setDisplayName(info1.name);
        meta1.setOwner(info1.name);
        skull1.setItemMeta(meta1);

        ItemStack r = new ItemStack(Material.STAINED_GLASS_PANE, 1,(short) 14);
        ItemMeta rm = r.getItemMeta();
        rm.setDisplayName("§c§l" + info.name + "にベトする");
        r.setItemMeta(rm);


        ItemStack b = new ItemStack(Material.STAINED_GLASS_PANE, 1,(short) 11);
        ItemMeta bm = b.getItemMeta();
        bm.setDisplayName("§9§l" + info1.name + "にベットする");
        b.setItemMeta(bm);


        bet.setItem(0, r);
        bet.setItem(1, r);
        bet.setItem(2, r);
        bet.setItem(9, r);
        bet.setItem(10, skull);
        bet.setItem(11, r);
        bet.setItem(18, r);
        bet.setItem(19, r);
        bet.setItem(20, r);

        bet.setItem(6, b);
        bet.setItem(7, b);
        bet.setItem(8, b);
        bet.setItem(15, b);
        bet.setItem(16, skull1);
        bet.setItem(17, b);
        bet.setItem(24, b);
        bet.setItem(25, b);
        bet.setItem(26, b);

        p.openInventory(bet);


    }

}
