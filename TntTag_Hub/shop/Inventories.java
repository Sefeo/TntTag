package TntTag_Hub.shop;

import TntTag_Hub.manager.Manager;
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

    private Manager manager;

    private ItemStack itemSpeed;
    private ItemStack itemSlow;
    private ItemStack itemInvul;
    private StringBuilder stringBuilder;

    public Inventories(Manager manager) {
        this.manager = manager;
    }

    public void createShopInv(Player p) { // Создание инвентаря магазина

        Inventory shop = Bukkit.createInventory(p, 9, "Магазин");
        p.openInventory(shop);

        manager.getInv().createSpeedItem(p);
        manager.getInv().createSlowItem(p);
        manager.getInv().createInvulItem(p);

        shop.setItem(2, manager.getInv().itemSpeed);
        shop.setItem(4, manager.getInv().itemSlow);
        shop.setItem(6, manager.getInv().itemInvul);
    }

    private void createSpeedItem(Player p){ // Создает предмет покупки скорости в магазине
        int speedPrice = manager.getBuffs().getItemPrice(p, "Speed");
        int speedValue = manager.getBuffs().getBuffValue(p, "Speed");

        List<String> loreSpeed = new ArrayList<>();
        itemSpeed = new ItemStack(Material.FEATHER);
        ItemMeta metaSpeed = itemSpeed.getItemMeta();
        metaSpeed.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (speedValue < 5) metaSpeed.setDisplayName(ChatColor.RESET + "Ускорение" + ChatColor.GREEN + " [" + speedValue + "/5]");
        else metaSpeed.setDisplayName(ChatColor.RESET + "Ускорение" + ChatColor.GREEN + " [МАКСИМУМ]");

        StringBuilder stringBuilder = new StringBuilder(ChatColor.RESET + "При получении удара у вас есть шанс ");
        switch(speedValue) {
            case 0:
                stringBuilder.append("5%");
                break;
            case 1:
                stringBuilder.append("7%");
                break;
            case 2:
                stringBuilder.append("9%");
                break;
            case 3:
                stringBuilder.append("11%");
                break;
            case 4: default:
                stringBuilder.append("13%");
                break;
        }
        loreSpeed.add(stringBuilder.toString());
        loreSpeed.add(ChatColor.RESET + "получить ускорение 1 секунду");

        if (speedValue < 5) loreSpeed.add(ChatColor.RESET + "Цена: §e" + speedPrice + "⭐");

        metaSpeed.setLore(loreSpeed);
        itemSpeed.setItemMeta(metaSpeed);
    }

    private void createSlowItem(Player p) { // Создает предмет покупки замедления в магазине
        int slowPrice = manager.getBuffs().getItemPrice(p, "Slow");
        int slowValue = manager.getBuffs().getBuffValue(p, "Slow");

        List<String> loreSlow = new ArrayList<>();
        itemSlow = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta metaSlow = itemSlow.getItemMeta();
        metaSlow.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (slowValue < 5) metaSlow.setDisplayName(ChatColor.RESET + "Замедление" + ChatColor.GREEN + " [" + slowValue + "/5]");
        else metaSlow.setDisplayName(ChatColor.RESET + "Замедление" + ChatColor.GREEN + " [МАКСИМУМ]");

        stringBuilder = new StringBuilder(ChatColor.RESET + "При нанесении удара у вас есть шанс ");
        switch(slowValue) {
            case 0:
                stringBuilder.append("5%");
                break;
            case 1:
                stringBuilder.append("7%");
                break;
            case 2:
                stringBuilder.append("9%");
                break;
            case 3:
                stringBuilder.append("11%");
                break;
            case 4: default:
                stringBuilder.append("13%");
                break;
        }
        loreSlow.add(stringBuilder.toString());
        loreSlow.add(ChatColor.RESET + "замедлить противника на 1 секунду");

        if (slowValue < 5) loreSlow.add(ChatColor.RESET + "Цена: §e" + slowPrice + "⭐");

        metaSlow.setLore(loreSlow);
        itemSlow.setItemMeta(metaSlow);
    }

    private void createInvulItem(Player p) { // Создает предмет покупки неуяза в магазине
        int invulPrice = manager.getBuffs().getItemPrice(p, "Invul");
        int invulValue = manager.getBuffs().getBuffValue(p, "Invul");

        List<String> loreInvul = new ArrayList<>();
        itemInvul = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta metaInvul = itemInvul.getItemMeta();
        metaInvul.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (invulValue < 5) metaInvul.setDisplayName(ChatColor.RESET + "Неуязвимость" + ChatColor.GREEN + " [" + invulValue + "/5]");
        else metaInvul.setDisplayName(ChatColor.RESET + "Неуязвимость" + ChatColor.GREEN + " [МАКСИМУМ]");

        stringBuilder = new StringBuilder(ChatColor.RESET + "После появления новых ТНТ у вас есть шанс ");
        switch(invulValue) {
            case 0:
                stringBuilder.append("1%");
                break;
            case 1:
                stringBuilder.append("2%");
                break;
            case 2:
                stringBuilder.append("3%");
                break;
            case 3:
                stringBuilder.append("4%");
                break;
            case 4: default:
                stringBuilder.append("5%");
                break;
        }
        loreInvul.add(stringBuilder.toString());
        loreInvul.add(ChatColor.RESET + "получить иммунитет к становлению ТНТ на 3 секунды");

        if (invulValue < 5) loreInvul.add(ChatColor.RESET + "Цена: §e" + invulPrice + "⭐");

        metaInvul.setLore(loreInvul);
        itemInvul.setItemMeta(metaInvul);
    }
}
