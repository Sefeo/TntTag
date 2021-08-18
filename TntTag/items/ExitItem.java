package TntTag.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ExitItem {

    public void giveExitItem(Player p) { p.getInventory().setItem(8, exitUse()); }

    public ItemStack exitUse() {
        ItemStack exit = new ItemStack(Material.COMPASS);
        ItemMeta meta = exit.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "Выйти в лобби");
        exit.setItemMeta(meta);
        return exit;
    }

}
