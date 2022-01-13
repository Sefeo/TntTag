package TntTag.listener;

import TntTag.items.ExitItem;
import TntTag.items.ShopItem;
import TntTag.manager.Manager;
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

    private List<Player> kicked = new ArrayList<>(); // переменная, обозначающая, что игрока кикнул именно сервер, а не он сам вышел, нужна в onDisconnect
    private Collection<? extends Player> playersList;

    private Manager manager;

    public Handler(Manager manager) {
        this.manager = manager;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        playersList = manager.getPlayers();
        int online = playersList.size();
        Player p = e.getPlayer();

        if (manager.getArena().roundStarted || manager.getArena().waitingTime <= 5) { // кикаем зашедшего игрока если игра уже началась
            kicked.add(p);
            p.sendMessage(ChatColor.RED + "Данная игра уже началась!");
            manager.getBungeeListener().connectToServer(p, "HUB");
            return;
        }

        e.setJoinMessage("Игрок §b" + p.getName() + "§f присоединился! (" + online + "/24)");
        Location loc = manager.getConfig().loc;
        p.teleport(loc);
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
        p.setFoodLevel(100);
        p.setLevel(0);

        manager.getArena().playersWithoutTNT = new ArrayList<>(playersList);
        manager.getArena().survivors = new ArrayList<>(playersList);
        manager.getArena().invulBool.put(p, false);

        shopItem.giveShop(p);
        exitItem.giveExitItem(p);

        switch(online) {
            case 1:
                manager.getScoreboard().updateSbNoPlayers(p); // если это единственный игрок, то показываем ему скорборд
                break;
            case 2:
                if(!manager.getArena().isWaitingTimerOn) manager.getArena().waitGame(); // если после захода их стало 2 - запускаем таймер, в waitGame есть отображение скорборда
                break;
            default:
                if(manager.getArena().waitingTime >= 30) manager.getArena().waitingTime -= 10; // снимаем по 10 секунд таймера за зашедшего игрока
                for (Player tempPlayers : playersList) manager.getScoreboard().updateSbInWaiting(tempPlayers);
                break;
        }
    }

    @EventHandler
    private void onDisconnect(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        playersList = manager.getPlayers();
        int online = playersList.size()-1; // онлайн без учёта ливнувшего

        if (manager.getArena().roundStarted) // если ливнул во время раунда
        {
            e.setQuitMessage(null);
            if (kicked.contains(p)) {
                kicked.remove(p);
                return; // если он не выбыл из игры, а был кикнут при входе, то просто ретурнимся
            }

            manager.getServer().broadcastMessage(ChatColor.RED + "Игрок " + p.getName() + " выбыл!");
            p.getWorld().createExplosion(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 4F, false, false);
            manager.getUpdateData().updateWinCount(p, false);

            manager.getArena().survivors.remove(p); // убираем из всех списков
            manager.getArena().tntList.remove(p);
            manager.getArena().playersWithoutTNT.remove(p);

            if(manager.getArena().survivors.size() == 1) manager.getArena().playerWin(manager.getArena().survivors.get(0)); // Если после лива остался один игрок, отдаем ему победу
            else if (manager.getArena().tntList.size() == 0 && manager.getArena().survivors.size() > 1)  // Если живых больше 1 и закончились ТНТ, запускаем след. раунд
                manager.getArena().startNextRound();

            for (Player tempPlayers : playersList) manager.getScoreboard().updateSbInGame(tempPlayers);

        } else { // если ливнул вне раунда
            e.setQuitMessage("Игрок §b" + p.getName() + "§f вышел! (" + online + "/24)");

            if (online <= 1) { // если после лива игроков 1 или меньше, выключаем таймер старта раунда и сбрасываем защиту от двойных таймеров
                Bukkit.getScheduler().cancelTask(manager.getArena().startingTimer);
                manager.getArena().isWaitingTimerOn = false;
                manager.getArena().waitingTime = 60;
            }
            else manager.getArena().waitingTime += 10; // если после лива осталось 2+, даем доп. секунды к таймеру

            for(Player tempPlayers: playersList) { // обновляем скорборды
                if(online > 1) manager.getScoreboard().updateSbInWaiting(tempPlayers);
                else manager.getScoreboard().updateSbNoPlayers(tempPlayers);
            }

        }
    }

    @EventHandler
    private void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player victim = (Player) e.getEntity();

            if (manager.getArena().roundStarted) // если удар был в раунде
            {
                int slowValue = manager.getBuffs().getBuffValue(damager, "Slow");
                int speedValue = manager.getBuffs().getBuffValue(victim, "Speed");

                if (didRandomWork("SPEED", speedValue)) { // карявый рандом навешивания эффектов
                    if(victim.hasPotionEffect(PotionEffectType.SLOW))
                        victim.removePotionEffect(PotionEffectType.SLOW); // если уже висит противоположный эффект, то просто очищаем
                    else victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
                }

                if (didRandomWork("SLOW", slowValue)) {
                    if (victim.hasPotionEffect(PotionEffectType.SPEED))
                        victim.removePotionEffect(PotionEffectType.SPEED);
                    else victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                }

                if (manager.getArena().isPlayerTnt(damager)) { // если ударяющим был ТНТ
                    if (manager.getArena().invulBool.get(victim)) { // если у жертвы временный неуяз, то посылаем атакующего нахуй
                        damager.sendMessage(ChatColor.RED + "У данного игрока временный иммунитет!");
                        e.setCancelled(true);
                        return;
                    }

                    manager.getArena().tntList.remove(damager);
                    manager.getArena().playersWithoutTNT.add(damager);
                    manager.getArena().tntList.add(victim);
                    manager.getArena().playersWithoutTNT.remove(victim); // свапаем атакующего и жертву в списках

                    damager.getInventory().clear();
                    damager.getInventory().setHelmet(null);
                    manager.getScoreboard().updateSbInGame(damager);

                    victim.getInventory().setHelmet(new ItemStack(Material.TNT));
                    victim.sendMessage(ChatColor.RED + "Передай динамит другому игроку, ударив его!");
                    victim.getInventory().setItem(0, new ItemStack(Material.TNT));
                    manager.getScoreboard().updateSbInGame(victim);
                }
            }
        }
    }

    private boolean didRandomWork(String buff, int buffValue) {
        buffValue += 1;
        if(buffValue > 5) buffValue = 5;
        int random = 1 + (int) (Math.random() * 100);

        if(buff.equals("SLOW")) return random <= manager.getConfig().slowBuffChance.get(buffValue);
        else return random <= manager.getConfig().speedBuffChance.get(buffValue);
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (item == null || item.getItemMeta() == null) return;

        if (item.getType().equals(Material.FEATHER) || item.getType().equals(Material.DIAMOND_SWORD) || item.getType().equals(Material.GOLDEN_APPLE))
            manager.getBuffs().buyBuff(p, item.getType()); // покупаем баф в зависимости от нажатого предмета

        e.setCancelled(true);
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null) return;

        if (item.getItemMeta().equals(shopItem.shopUse().getItemMeta())) manager.getInv().createShopInv(p); // открываем окно магазина
        else if (item.getItemMeta().equals(exitItem.exitUse().getItemMeta())) { // компас, отправляем в хаб и даем хабовский скорборд
            manager.getBungeeListener().connectToServer(p, "HUB");
            manager.getScoreboard().updateSbNoPlayers(p);
        }
        e.setCancelled(true);
    }

}