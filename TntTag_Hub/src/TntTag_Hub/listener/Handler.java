package TntTag_Hub.listener;

import TntTag_Hub.items.ServerJoinItem;
import TntTag_Hub.items.ShopItem;
import TntTag_Hub.manager.Manager;
import TntTag_Hub.scoreboard.Scoreboard;
import TntTag_Hub.shop.Buffs;
import TntTag_Hub.shop.Inventories;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class Handler implements Listener {

    private Inventory serverChooseInv;  // инвентарь выбора серверов
    int serversUpdateTimer; // ежесекундный таймер обновления серверов

    private Manager manager;
    private BungeeListener bungeeListener;
    private ShopItem shopItem;
    private ServerJoinItem joinItem;
    private Buffs buffs;
    private Inventories inventories;
    private Scoreboard scoreboard;

    public Handler(Manager manager) {
        this.manager = manager;
        this.bungeeListener = manager.getBungeeListener();
        this.shopItem = manager.getShopItem();
        this.joinItem = manager.getJoinItem();
        this.buffs = manager.getBuffs();
        this.inventories = manager.getInv();
        this.scoreboard = manager.getScoreboard();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        manager.getCheckData().checkPlayerData(p); // проверка на то, есть ли игрок в БД

        e.setJoinMessage(null);
        p.setInvulnerable(true);
        p.setGameMode(GameMode.ADVENTURE);
        Location loc = new Location(p.getWorld(), 214, 71, 69, -180, -1.5F);
        p.teleport(loc);
        p.setFoodLevel(100);
        p.setHealth(20F);
        p.setLevel(0);
        p.setExp(0F);

        p.getInventory().clear();
        shopItem.giveShop(p);

        if(manager.getPlayers().size() == 1) // врубаем таймер только если зашел первый игрок
        {
            serversUpdateTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () -> {
                bungeeListener.updateServersInfo(); // обновляем каждую секунду инфу о серверах
                for(Player pl: manager.getPlayers()) {
                    if (p.getOpenInventory().getTopInventory().equals(serverChooseInv))
                        joinItem.createJoinItem(pl, serverChooseInv);
                }
            }, 20L, 20);
        }
    }

    @EventHandler
    public void onDisc(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if(manager.getPlayers().size() == 1)
            Bukkit.getScheduler().cancelTask(serversUpdateTimer); // если вышел единственный игрок, то прерываем обновление серверов
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (item == null || item.getItemMeta() == null) return;

        if (item.getType().equals(Material.FEATHER) || item.getType().equals(Material.DIAMOND_SWORD) || item.getType().equals(Material.GOLDEN_APPLE)) {
            buffs.buyBuff(p, item.getType()); // покупаем баф в зависимости от нажатого предмета
            scoreboard.updateScoreboard(p);
        }

        if(item.getType().equals(Material.TNT) && item.getItemMeta().getDisplayName() != null) {
            bungeeListener.connectToServer(p, item.getItemMeta().getDisplayName()); // подключаем игрока к нажатому серверу
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null) return;
        if (item.getItemMeta().equals(shopItem.shopUse().getItemMeta()))
            inventories.createShopInv(p); // открываем окно магазина
        e.setCancelled(true);
    }


    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player p = e .getPlayer();
        Entity ent = e.getRightClicked();

        if(ent.getCustomName().equals(ChatColor.RED + "TNT TAG")) { // если игрок жмёт по нужному НПС
            serverChooseInv = Bukkit.createInventory(p, 9, "Сервера");
            p.openInventory(serverChooseInv); // открываем ему инвентарь для выбора сервера

            joinItem.createJoinItem(p, serverChooseInv); // и закидываем туда предметы
        }
        e.setCancelled(true);
    }
}
