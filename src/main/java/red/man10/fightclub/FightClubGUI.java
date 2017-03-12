package red.man10.fightclub;


import org.bukkit.ChatColor;

import org.bukkit.enchantments.Enchantment;

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
       Player p = (Player) e.getWhoClicked();
        //try {
        /*
        if(e.getClickedInventory().getTitle().equals("§9§lプレイヤーを登録する")){
            String pageNumberString = e.getClickedInventory().getItem(49).getItemMeta().getLore().get(0);
            int pageNumberInt = Integer.parseInt(pageNumberString);
            if(e.getSlot() == 48){
                    if (pageNumberInt == 0) {
                        //1ページ目なら何もしない
                        e.setCancelled(true);
                        return;
                    } else {
                        //ちがければ-1ページ
                        registerPlayerGUI(p, pageNumberInt - 1);
                    //left
                }
            }
            if(e.getSlot() == 50){
                registerPlayerGUI(p,pageNumberInt+1);
                //right
            }
            if(e.getSlot() == 49){
            }
            if(e.getCurrentItem().getType() == Material.SKULL_ITEM){
                String clickedFighter = e.getCurrentItem().getItemMeta().getDisplayName();
                Player fighter = Bukkit.getPlayer(clickedFighter);




                if(plugin.registerFighter(fighter.getUniqueId(), fighter.getName()) == -1){
                    fighter.sendMessage("プレイヤーは登録できません");
                }
            }

            e.setCancelled(true);
            return;
        }
        if(e.getClickedInventory().getTitle().equals("§c§lMFC Admin Console")) {
            if(e.getSlot() == 10){
                registerPlayerGUI(p,0);
                return;
            }
            if(e.getSlot() == 12){
                plugin.tp(p,plugin.selectedArena,"spawn");
            }
            if(e.getSlot() == 16){

            }
            if(e.getSlot() == 29){
                plugin.guiBetMenu((Player) e.getWhoClicked());
            }
            if(e.getSlot() == 31){

            }
            if(e.getSlot() == 33){
            }

            e.setCancelled(true);
            return;
        }
        */
        if (e.getInventory().getTitle().equals("§c§l         ベットメニュー")) {
            FightClub.PlayerInformation info = plugin.fighters.get(0);
            FightClub.PlayerInformation info1 = plugin.fighters.get(1);
            if (e.getCurrentItem().getType() == Material.SKULL_ITEM) {
                priceMenu(p, e.getCurrentItem().getItemMeta().getDisplayName());
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
            }

        }
        if(e.getClickedInventory().getTitle().equals("     §cMan10 Fight Club menu")){
            if(e.getSlot() == 1){


                //選手登録処理
                int ret = plugin.registerFighter(e.getWhoClicked().getUniqueId(), e.getWhoClicked().getName());
                if(ret == -1){
                    p.sendMessage("対戦者リストにすでに登録されています");
                }
                else if(ret == -2){
                    p.sendMessage("観戦者リストにすでに登録されています");
                }
                else if(ret == -3){
                    p.sendMessage("参加費がしはらえないため参加できません");
                }else{
                    p.sendMessage("選手登録しました");
                }


            }

            if(e.getSlot() == 2){
                //登録をキャンセル処理
                plugin.unregisterFighter(e.getWhoClicked().getUniqueId());
                p.sendMessage("参加をとりやめました");
            }
            if(e.getSlot() == 4){
                plugin.guiBetMenu((Player) e.getWhoClicked());
            }
            if(e.getSlot() == 6){
                //  観戦者登録
                int ret = plugin.registerSpectator(p.getUniqueId());
                if(ret == -1){
                    p.sendMessage("対戦者リストにすでに登録されています");
                }
                if(ret == -2){
                    p.sendMessage("観戦者リストにすでに登録されています");
                }
            }
            if(e.getSlot() == 7){
                //観戦から戻る処理
                plugin.unregisterSpectator(p.getUniqueId());
            }

            e.setCancelled(true);
            return;
        }
            if (e.getInventory().getItem(52).getItemMeta().getDisplayName().equalsIgnoreCase("§c§lキャンセル")) {
                String val = e.getClickedInventory().getItem(50).getItemMeta().getLore().get(1);
                if (val.length() <= 8) {
                    if (e.getSlot() == 46) { //0
                        if (e.getInventory().getItem(50).getItemMeta().getLore().get(1).length() > 0) {
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
                        if(e.getInventory().getItem(50).getItemMeta().getLore().get(1) == ""){
                            p.sendMessage("掛け金を入力してください");
                            e.setCancelled(true);
                            return;
                        }else{
                            placeBetGUI(e.getInventory(), p);
                            e.setCancelled(true);
                            return;
                        }
                        //確認処理
                    }
                    e.setCancelled(true);
                } else {
                    if (e.getSlot() == 48) {
                        clearCalc(e.getInventory());
                    } else if (e.getSlot() == 50) {
                        e.setCancelled(true);
                        int money = Integer.parseInt(e.getInventory().getItem(50).getItemMeta().getLore().get(1));
                        if(e.getClickedInventory().getItem(50).getItemMeta().getLore().get(1) == null){
                            p.sendMessage("掛け金をかけてください。");
                            e.setCancelled(true);
                        }else{
                            placeBetGUI(e.getInventory(), p);
                        }
                        return;

                    } else if (e.getSlot() == 52) {
                        p.closeInventory();
                    } else {
                        e.setCancelled(true);
                        p.sendMessage("上限！！");
                    }
                    e.setCancelled(true);
                }

            }
            return;
        }
     // }catch (Exception ee){



    //##################[MFC BET MENU]#####################
    void placeBetGUI(Inventory i, Player p){
        int money = Integer.parseInt(i.getItem(50).getItemMeta().getLore().get(1)); //設定したbal

        String fighterName = i.getItem(33).getItemMeta().getDisplayName();
        Player fighterPlayer = Bukkit.getPlayer(fighterName);

        UUID fighter = fighterPlayer.getUniqueId(); //    ShoへここへターゲットのUUID

        String buyer = p.getName();
        p.sendMessage(buyer);




        if(plugin.canBet(p.getUniqueId()) == false){
            p.closeInventory();
            return;
        }


        double balance = plugin.vault.getBalance(p.getUniqueId());
        p.sendMessage("あなたの残額は $"+balance +"です");
        if(balance < money){
            p.sendMessage(ChatColor.RED+ "残高が足りません！！");
            p.closeInventory();
            return;
        }

        if(plugin.vault.withdraw(p.getUniqueId(),money) == false){
            p.sendMessage(ChatColor.RED+ "お金の引き出しに失敗しました" );
            p.closeInventory();
            return;
        }

        if(plugin.betFighter(fighter,money,p.getUniqueId(),buyer) == -1){
            p.sendMessage(ChatColor.RED+"選手はベットできません");
            p.closeInventory();
            return;
        }

        if(plugin.betFighter(fighter,money,p.getUniqueId(),buyer) == -2){
            p.sendMessage(ChatColor.RED+"投票受付中しかベットできません");
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

        FightClub.PlayerInformation info = plugin.fighters.get(0);
        FightClub.PlayerInformation info1 = plugin.fighters.get(1);

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
        rm.setDisplayName("§c§l" + info.name + "にベットする");
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
//#########################################################

    public void createJoinmenu(Player p){
        Inventory i = Bukkit.createInventory(null, 9, "     §cMan10 Fight Club menu");

        ItemStack ticket = new ItemStack(Material.PAPER);
        ItemMeta ticketmeta = ticket.getItemMeta();
        ticketmeta.setDisplayName("§a§lMFC選手登録");
        ticket.setItemMeta(ticketmeta);

        ItemStack quit = new ItemStack(Material.TNT);
        ItemMeta quitmeta = quit.getItemMeta();
        quitmeta.setDisplayName("§c§l登録をキャンセル");
        quit.setItemMeta(quitmeta);

        ItemStack watch = new ItemStack(Material.EYE_OF_ENDER);
        ItemMeta watchmeta = watch.getItemMeta();
        watchmeta.setDisplayName("§5§l観戦");
        watch.setItemMeta(watchmeta);

        ItemStack watchback = new ItemStack(Material.MINECART);
        ItemMeta watchbackmeta = watchback.getItemMeta();
        watchbackmeta.setDisplayName("§7§lロビーに戻る");
        watchback.setItemMeta(watchbackmeta);

        ItemStack bet = new ItemStack(Material.FLOWER_POT_ITEM);
        ItemMeta betmeta = bet.getItemMeta();
        betmeta.addEnchant(Enchantment.ARROW_FIRE,1,true);
        betmeta.setDisplayName("§c§lベットする");
        bet.setItemMeta(betmeta);

        i.setItem(1, ticket);
        i.setItem(2, quit);
        i.setItem(4, bet);
        i.setItem(6, watch);
        i.setItem(7, watchback);

        p.openInventory(i);

    }
/*
    public void adminMenu(Player p){
        Inventory i = Bukkit.createInventory(null, 45, "§c§lMFC Admin Console");
        addServerPlayerList();

        ItemStack reg = new ItemStack(Material.PAPER);
        ItemMeta regmeta = reg.getItemMeta();
        regmeta.setDisplayName("§a§l選手を登録");
        regmeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        reg.setItemMeta(regmeta);

        ItemStack bet = new ItemStack(Material.EMERALD);
        ItemMeta betmeta = bet.getItemMeta();
        betmeta.setDisplayName("§6§lベットする");
        betmeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        bet.setItemMeta(betmeta);

        ItemStack spec = new ItemStack(Material.EYE_OF_ENDER);
        ItemMeta specmeta = spec.getItemMeta();
        specmeta.setDisplayName("§7§l観戦する");
        specmeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        spec.setItemMeta(specmeta);

        ItemStack cancelreg = new ItemStack(Material.BARRIER);
        ItemMeta cancelregmeta = cancelreg.getItemMeta();
        cancelregmeta.setDisplayName("§c§l登録を取り消しする");
        cancelregmeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        cancelreg.setItemMeta(cancelregmeta);

        ItemStack sets = new ItemStack(Material.ANVIL);
        ItemMeta setsmeta = sets.getItemMeta();
        setsmeta.setDisplayName("§d§lゲームのステートを変更する");
        setsmeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        sets.setItemMeta(setsmeta);

        ItemStack cancelgame = new ItemStack(Material.TNT);
        ItemMeta cancelgamemeta = cancelgame.getItemMeta();
        cancelgamemeta.setDisplayName("§4§lゲームをキャンセルする");
        cancelgamemeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        cancelgame.setItemMeta(cancelgamemeta);

        ItemStack start = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta startmeta = start.getItemMeta();
        startmeta.setDisplayName("§b§lゲームを始める");
        startmeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        start.setItemMeta(startmeta);

        i.setItem(10, reg);
        i.setItem(12, spec);
        i.setItem(14, sets);
        i.setItem(16, start);

        i.setItem(29, bet);
        i.setItem(31, cancelreg);
        i.setItem(33, cancelgame);


        p.openInventory(i);

    }

    public void changeState(Player p){
        Inventory i = Bukkit.createInventory(null, 9, "ゲームステートを変更する");
        
    }

    ArrayList<UUID> serverPlayerList = new ArrayList<UUID>();

    public void addServerPlayerList(){
        serverPlayerList.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            serverPlayerList.add(player.getUniqueId());
        }
    }

    public void registerPlayerGUI(Player p,int page){
        Inventory i = Bukkit.createInventory(null, 54, "§9§lプレイヤーを登録する");
        int c = 0;
        int actualPage = page * 44;

        ArrayList<String> sign = new ArrayList<String>();
        sign.add(String.valueOf(page));
        int slot = 0;

        ItemStack pageitem = new ItemStack(Material.SIGN);
        ItemMeta pageitemMeta = pageitem.getItemMeta();
        pageitemMeta.setDisplayName("§2§l" +String.valueOf(page) + "ページ");
        pageitemMeta.setLore(sign);
        pageitem.setItemMeta(pageitemMeta);
        ItemStack left = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23").withName("前").build();
        ItemStack right = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b").withName("次").build();
        i.setItem(49, pageitem);
        i.setItem(48, left);
        i.setItem(50, right);
        p.openInventory(i);

        while(c <= 44){
            Player pp = Bukkit.getPlayer(serverPlayerList.get(actualPage + c));
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setDisplayName(pp.getName());
            meta.setOwner(pp.getName());
            skull.setItemMeta(meta);
            i.setItem(slot, skull);
            slot++;
            c++;
        }


    }*/

}
