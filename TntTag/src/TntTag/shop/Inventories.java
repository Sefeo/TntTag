package TntTag.shop;

import TntTag.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Inventories {

    private ItemStack itemSpeed;
    private ItemStack itemSlow;
    private ItemStack itemInvul;

    private Manager manager;

    public Inventories(Manager manager) {
        this.manager = manager;
    }

    public void createShopInv(Player p) { // Создание инвентаря магазина

        Inventory shop = Bukkit.createInventory(p, 9, "Магазин");
        p.openInventory(shop);

        createSpeedItem(p);
        createSlowItem(p);
        createInvulItem(p);

        shop.setItem(2, itemSpeed);
        shop.setItem(4, itemSlow);
        shop.setItem(6, itemInvul);
    }

    private void createSpeedItem(Player p){
        int speedPrice = manager.getBuffs().getItemPrice(p, "Speed");
        int speedValue = manager.getBuffs().getBuffValue(p, "Speed");

        List<String> loreSpeed = new ArrayList<>();
        itemSpeed = new ItemStack(Material.FEATHER);
        ItemMeta metaSpeed = itemSpeed.getItemMeta();
        metaSpeed.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (speedValue < 5) metaSpeed.setDisplayName(ChatColor.RESET + "Ускорение" + ChatColor.GREEN + " [" + speedValue + "/5]");
        else metaSpeed.setDisplayName(ChatColor.RESET + "Ускорение" + ChatColor.GREEN + " [МАКСИМУМ]");

        int loreSpeedValue = speedValue+1; // очень костыльно, но я заебался уже с этими значениями
        if(loreSpeedValue > 5) loreSpeedValue = 5;
        loreSpeed.add(ChatColor.RESET + "При получении удара у вас есть шанс " + manager.getConfig().speedBuffChance.get(loreSpeedValue) + "%");
        loreSpeed.add(ChatColor.RESET + "получить ускорение 1 секунду");
        if (speedValue < 5) loreSpeed.add(ChatColor.RESET + "Цена: §e" + speedPrice + "⭐");

        metaSpeed.setLore(loreSpeed);
        itemSpeed.setItemMeta(metaSpeed);
    }

    private void createSlowItem(Player p) {
        int slowPrice = manager.getBuffs().getItemPrice(p, "Slow");
        int slowValue = manager.getBuffs().getBuffValue(p, "Slow");

        List<String> loreSlow = new ArrayList<>();
        itemSlow = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta metaSlow = itemSlow.getItemMeta();
        metaSlow.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (slowValue < 5) metaSlow.setDisplayName(ChatColor.RESET + "Замедление" + ChatColor.GREEN + " [" + slowValue + "/5]");
        else metaSlow.setDisplayName(ChatColor.RESET + "Замедление" + ChatColor.GREEN + " [МАКСИМУМ]");

        int loreSlowValue = slowValue+1;
        if(loreSlowValue > 5) loreSlowValue = 5;
        loreSlow.add(ChatColor.RESET + "При нанесении удара у вас есть шанс " + manager.getConfig().slowBuffChance.get(loreSlowValue) + "%");
        loreSlow.add(ChatColor.RESET + "замедлить противника на 1 секунду");
        if (slowValue < 5) loreSlow.add(ChatColor.RESET + "Цена: §e" + slowPrice + "⭐");

        metaSlow.setLore(loreSlow);
        itemSlow.setItemMeta(metaSlow);
    }

    private void createInvulItem(Player p) {
        int invulPrice = manager.getBuffs().getItemPrice(p, "Invul");
        int invulValue = manager.getBuffs().getBuffValue(p, "Invul");
        if(invulValue > 5) invulValue = 5;

        List<String> loreInvul = new ArrayList<>();
        itemInvul = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta metaInvul = itemInvul.getItemMeta();
        metaInvul.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (invulValue < 5) metaInvul.setDisplayName(ChatColor.RESET + "Неуязвимость" + ChatColor.GREEN + " [" + invulValue + "/5]");
        else metaInvul.setDisplayName(ChatColor.RESET + "Неуязвимость" + ChatColor.GREEN + " [МАКСИМУМ]");

        int loreInvulValue = invulValue+1;
        if(loreInvulValue > 5) loreInvulValue = 5;
        loreInvul.add(ChatColor.RESET + "После появления новых ТНТ у вас есть шанс " + manager.getConfig().invulBuffChance.get(loreInvulValue) + "%");
        loreInvul.add(ChatColor.RESET + "получить иммунитет к становлению ТНТ на 3 секунды");
        if (invulValue < 5) loreInvul.add(ChatColor.RESET + "Цена: §e" + invulPrice + "⭐");

        metaInvul.setLore(loreInvul);
        itemInvul.setItemMeta(metaInvul);
    }

}
