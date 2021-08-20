package TntTag.listener;

import TntTag.arena.Arena;
import TntTag.items.ExitItem;
import TntTag.items.ShopItem;
import TntTag.manager.Manager;
import TntTag.scoreboard.Scoreboard;
import TntTag.shop.Buffs;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Handler implements Listener {

    private ShopItem shopItem = new ShopItem();
    private ExitItem exitItem = new ExitItem();

    private List<Player> kicked = new ArrayList<>(); // переменная, обозначающая, что игрока кикнул именно сервер, а не он сам вышел, нужна в onDisc
    private Collection<? extends Player> playersList;

    private Manager manager;
    private Arena arena;
    private Scoreboard scoreboards;
    private Buffs buffs;
    private BungeeListener bungeeListener;

    public Handler(Manager manager) {
        this.manager = manager;
        this.arena = manager.getArena();
        this.scoreboards = manager.getScoreboard();
        this.buffs = manager.getBuffs();
        this.bungeeListener = manager.getBungeeListener();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        playersList = manager.getPlayers();
        int online = playersList.size();
        Player p = e.getPlayer();

        if (arena.roundStarted || arena.waitingTime <= 5) { // кикаем зашедшего игрока если игра уже началась
            kicked.add(p);
            p.sendMessage(ChatColor.RED + "Данная игра уже началась!");
            bungeeListener.connectToServer(p, "HUB");
            return;
        }

        e.setJoinMessage("Игрок §b" + p.getName() + "§f присоединился! (" + online + "/24)");
        Location loc = new Location(p.getWorld(), 1, 29, 1);
        p.teleport(loc);
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
        p.setFoodLevel(100);
        p.setLevel(0);

        arena.playersWithoutTNT = new ArrayList<>(playersList);
        arena.survivors = new ArrayList<>(playersList);
        arena.invulBool.put(p, false);

        shopItem.giveShop(p);
        exitItem.giveExitItem(p); // даем предметы магаза и выхода

        switch(online) {
            case 1:
                scoreboards.updateSbNoPlayers(p); // если это единственный игрок, то показываем ему скорборд
                break;
            case 2:
                if(!arena.isWaitingTimerOn) arena.waitGame(); // если после захода их стало 2 - запускаем таймер, в waitGame есть отображение скорборда
                break;
            default:
                if(arena.waitingTime >= 30) arena.waitingTime -= 10; // снимаем по 10 секунд таймера за зашедшего игрока
                for (Player tempPlayers : playersList) scoreboards.updateSbInWaiting(tempPlayers); // обновляем скорборды для всех
                break;
        }
    }

    @EventHandler
    public void onDisc(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        playersList = manager.getPlayers();
        int online = playersList.size()-1; // онлайн без учёта ливнувшего

        if (arena.roundStarted) { // если ливнул во время раунда
            e.setQuitMessage(null);
            if (kicked.contains(p)) return; // если он не выбыл из игры, а был кикнут при входе, то просто ретурнимся

            manager.getServer().broadcastMessage(ChatColor.RED + "Игрок " + p.getName() + " выбыл!");
            p.getWorld().createExplosion(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 4F, false, false);
            manager.getUpdateData().updateWinCount(p, false);

            arena.survivors.remove(p);
            arena.tntList.remove(p);
            arena.playersWithoutTNT.remove(p); // убираем из всех списков

            if(arena.playersWithoutTNT.size() == 1) arena.playerWin(arena.playersWithoutTNT.get(0)); // если остался 1 живой не ТНТ, он побеждает
            else if(arena.survivors.size() == 1) arena.playerWin(arena.survivors.get(0)); // Если нет игроков без ТНТ, то победу отдаем оставшемуся ТНТ
            else if (arena.tntList.size() == 0 && arena.survivors.size() > 1) { // Если живых больше 1 и закончились ТНТ, то выбираем новых ТНТ
                arena.chooseTNTs();
                arena.waitingTime = 60;
            }
            for (Player tempPlayers : playersList) scoreboards.updateSbInGame(tempPlayers); // обновляем скорборды для всех в раунде
        } else { // если ливнул вне раунда
            e.setQuitMessage("Игрок §b" + p.getName() + "§f вышел! (" + online + "/24)");
            if (online <= 1) { // если после лива остался 1 игрок или 0, то выключаем таймер старта раунда и сбрасываем защиту от двойных таймеров
                Bukkit.getScheduler().cancelTask(arena.startingTimer);
                arena.isWaitingTimerOn = false;
                arena.waitingTime = 60;
                if(online == 1) {
                    for(Player lastPlayer: playersList) scoreboards.updateSbNoPlayers(lastPlayer); // обновляем скорборд если остался хоть 1 игрок
                }
            } else { // если после лива осталось 2+ игроков
                arena.waitingTime += 10; // даем доп. секунды к таймеру
                for(Player tempPlayers: playersList) scoreboards.updateSbInWaiting(tempPlayers); // обновляем для всех скорборды
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) { // если именно игрок ударил именно игрока
            Player damager = (Player) e.getDamager();
            Player victim = (Player) e.getEntity();

            if (arena.roundStarted) { // если удар был в раунде
                int slow = buffs.getBuffValue(damager, "Slow");
                int speed = buffs.getBuffValue(victim, "Speed");
                int randSpeed = 1 + (int) (Math.random() * 100);
                int randSlow = 1 + (int) (Math.random() * 100);

                if (speed == 1 && randSpeed < 6 || speed == 2 && randSpeed < 8 || speed == 3 && randSpeed < 10 || speed == 4 && randSpeed < 12 || speed == 5 && randSpeed < 14) { // карявый рандом навешивания скорости
                    if(victim.hasPotionEffect(PotionEffectType.SLOW)) {
                        victim.removePotionEffect(PotionEffectType.SLOW); // если уже висит противоположный эффект, то просто очищаем
                        return;
                    }
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
                }

                if (slow == 1 && randSlow < 6 || slow == 2 && randSlow < 8 || slow == 3 && randSlow < 10 || slow == 4 && randSlow < 12 || slow == 5 && randSlow < 14) { // карявый рандом навешивания замедления
                    if (victim.hasPotionEffect(PotionEffectType.SPEED)) {
                        victim.removePotionEffect(PotionEffectType.SPEED); // если уже висит противоположный эффект, то просто очищаем
                        return;
                    }
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                }

                if (arena.tntList.contains(damager)) { // если ударяющим был ТНТ
                    if (arena.invulBool.get(victim)) { // если у жертвы временный неуяз, то посылаем атакующего нахуй
                        damager.sendMessage(ChatColor.RED + "У данного игрока временный иммунитет!");
                        e.setCancelled(true);
                        return;
                    }

                    arena.tntList.remove(damager);
                    arena.playersWithoutTNT.add(damager);
                    arena.tntList.add(victim);
                    arena.playersWithoutTNT.remove(victim); // свапаем атакующего и жертву в списках

                    damager.getInventory().clear();
                    damager.getInventory().setHelmet(null);
                    scoreboards.updateSbInGame(damager);

                    victim.getInventory().setHelmet(new ItemStack(Material.TNT));
                    victim.sendMessage(ChatColor.RED + "Передай динамит другому игроку, ударив его!");
                    victim.getInventory().setItem(0, new ItemStack(Material.TNT));
                    scoreboards.updateSbInGame(victim);
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (item == null || item.getItemMeta() == null) return;
        if (item.getType().equals(Material.FEATHER) || item.getType().equals(Material.DIAMOND_SWORD) || item.getType().equals(Material.GOLDEN_APPLE)) {
            buffs.buyBuff(p, item.getType()); // покупаем баф в зависимости от нажатого предмета
            if(arena.isWaitingTimerOn) scoreboards.updateSbInWaiting(p); // и обновляем скорборды
            else scoreboards.updateSbNoPlayers(p);
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null) return;
        if (item.getItemMeta().equals(shopItem.shopUse().getItemMeta())) manager.getInv().createShopInv(p); // открываем окно магазина
        else if (item.getItemMeta().equals(exitItem.exitUse().getItemMeta())) { // компас, отправляем в хаб и даем хабовский скорборд
            bungeeListener.connectToServer(p, "HUB");
            scoreboards.updateSbNoPlayers(p);
        }
        e.setCancelled(true);
    }

}