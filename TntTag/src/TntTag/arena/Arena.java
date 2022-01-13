package TntTag.arena;

import TntTag.manager.Manager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Arena {

    private Manager manager;

    public Arena(Manager manager) {
        this.manager = manager;
    }

    public int waitingTime = 60;
    public int round;
    public int startingTimer;

    private int inGameTimer;
    private int firework;
    private int earnedMoney;

    private Collection<? extends Player> playersList; // вообще все игроки  на сервере, в том числе наблюдатели
    public List<Player> tntList = new ArrayList<>();
    public List<Player> playersWithoutTNT;
    public List<Player> survivors;

    public boolean roundStarted = false;
    public boolean isWaitingTimerOn = false; // предотвращает двойной запуск таймеров
    public HashMap<Player, Boolean> invulBool = new HashMap<>(); // указывает, что игрок временно неуязвим, используется в onHit

    public boolean isPlayerTnt(Player p) {
        return tntList.contains(p);
    }

    private void chooseTNTs() { // выбор ТНТ при начале раунда
        Random r = new Random();
        int rt = r.nextInt(playersWithoutTNT.size());
        int tntCount = (int) Math.round(playersWithoutTNT.size() * 0.3); // рандомные ТНТ исходя из кол-ва игроков

        for (int i = 0; i < tntCount; i++) {
            Player tnt = playersWithoutTNT.get(rt);
            tnt.getInventory().setHelmet(new ItemStack(Material.TNT));
            tnt.sendMessage(ChatColor.RED + "Передай динамит другому игроку, ударив его!");
            tnt.getInventory().setItem(0, new ItemStack(Material.TNT));
            tntList.add(tnt);
            playersWithoutTNT.remove(tnt);
        }

        for(Player p : survivors) manager.getScoreboard().updateSbInGame(p);

        for (Player p : playersWithoutTNT) { // рандом неуяза для всех НЕ тнт
            int invulValue = manager.getBuffs().getBuffValue(p, "Invul")+1;
            if(invulValue > 5) invulValue = 5;
            int random = 1 + (int) (Math.random() * 100);

            if(random <= manager.getConfig().speedBuffChance.get(invulValue))
                invulBool.put(p, true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> invulBool.put(p, false), 60L); // таймер на снятие неуязвимости
        }
    }

    public void waitGame() { // ожидание начала раунда, запускается в хендлере
        playersList = manager.getPlugin().getServer().getOnlinePlayers();

        assert (playersList.size() != 0);

        if (playersList.size() < 24) waitingTime = (24-playersList.size()) * 10; // добавляем по 10 секунд ожидания за каждый пустой слот
        else waitingTime = 20;

        for (Player p : playersList) manager.getScoreboard().updateSbInWaiting(p);

        if(isWaitingTimerOn) return; // Когда onJoin вызывает этот метод и таймер уже запущен, то обновляется только скорборд
        isWaitingTimerOn = true;

        startingTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () -> {
            switch(waitingTime) {
                case 20: case 10:
                    manager.getPlugin().getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунд!");
                    break;
                case 5:
                    manager.getPlugin().getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунд!");
                    for (Player p : playersList) p.getInventory().clear();
                    MinecraftServer.getServer().setMotd("RoundStartedTrue");
                    break;
                case 2: case 3: case 4:
                    manager.getPlugin().getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунды!");
                    break;
                case 1:
                    manager.getPlugin().getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунда!");
                    break;
                case 0:
                    waitingTime = 60; // теперь waitingTime показывает время до взрыва ТНТ
                    for (Player p : playersList) {
                        if(!survivors.contains(p)) survivors.add(p);
                        if(!playersWithoutTNT.contains(p)) playersWithoutTNT.add(p); // добавляем всех игроков в списки
                    }
                    startRound();
                    roundStarted = true;
                    manager.getPlugin().getServer().broadcastMessage("§aИгра началась!");
                    Bukkit.getScheduler().cancelTask(startingTimer);
                    round = 1;
                    chooseTNTs();
                    break;
            }
            waitingTime--;
            for (Player p : playersList) manager.getScoreboard().updateSbInWaiting(p); // обновляем скорборды для всех каждую секунду таймера
        }, 20L, 20);
    }

    private void startRound() {
        playersList = manager.getPlayers();

        inGameTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () -> { // ежесекундный таймер в раунде
            if (!roundStarted) return;

            waitingTime--; // теперь waitingTime показывает время до взрыва ТНТ, 60 секунд

            for (Player p : survivors) {
                p.setLevel(waitingTime); // лвл отображает время до взрыва
                manager.getScoreboard().updateSbInGame(p);
            }

            if (waitingTime == 0) { // взрываем всех ТНТ когда таймер доходит до нуля
                for(Player p: tntList) {

                    p.getWorld().createExplosion(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 4F, false, false);
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage(ChatColor.YELLOW + "Ты проиграл!");

                    earnedMoney = (round - 1) * 50; // каждый раунд по 50 монеток
                    if (earnedMoney > 0) {
                        p.sendMessage("Заработано: §e" + earnedMoney + "⭐");
                        manager.getUpdateData().updatePlayerMoney(p, earnedMoney, true);
                    }

                    p.getInventory().clear();
                    p.removePotionEffect(PotionEffectType.SPEED);

                    playersWithoutTNT.remove(p);
                    survivors.remove(p);

                    manager.getUpdateData().updateWinCount(p, false);
                    manager.getScoreboard().updateSbNoPlayers(p);

                    if(survivors.size() == 1) playerWin(survivors.get(0)); // если выживший один - даем победу, если нет - новый раунд
                    else startNextRound();
                }

                tntList.clear();
                Bukkit.getScheduler().cancelTask(inGameTimer);
            }

        }, 20L, 20);
    }

    public void startNextRound() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> { // ждём 3с перед началом нового раунда
            round++;
            waitingTime = 60 - ((round - 1) * 5); // с каждым раундом время до взрыва уменьшается на 5с

            for (Player p : survivors) {
                manager.getScoreboard().updateSbInGame(p);
                if (survivors.size() < 7) p.teleport(manager.getConfig().loc); // тпаем всех в центр и ждём 3с перед новыми ТНТ
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> {
                startRound();
                chooseTNTs();
            }, 60L);

        }, 60L);
    }

    public void playerWin(Player winner) {
        roundStarted = false;

        System.out.println("Winner - " + winner);

        winner.getInventory().clear();
        winner.setLevel(0);
        winner.setAllowFlight(true);
        winner.setFlying(true);

        manager.getUpdateData().updateWinCount(winner, true);
        earnedMoney = round * 50 + 100; // по 50 монет за каждый раунд, 100 за саму победу
        manager.getUpdateData().updatePlayerMoney(winner, earnedMoney, true);
        round = 1;

        waitingTime = 8; // 8 секунд до выброса в лобби
        for (Player p : playersList) manager.getScoreboard().updateSbInWaiting(p);
        isWaitingTimerOn = false; // сбрасываем защиту от двойного таймера

        firework = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () -> {

            Firework fw = (Firework) winner.getWorld().spawnEntity(winner.getLocation().clone().subtract(0, 0, -1), EntityType.FIREWORK);
            Firework fw2 = (Firework) winner.getWorld().spawnEntity(winner.getLocation().clone().subtract(-1, 0, 0), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();

            Random r = new Random();

            int rt = r.nextInt(3) + 1;
            FireworkEffect.Type t;
            if (rt == 1) t = FireworkEffect.Type.BALL;
            else if (rt == 2) t = FireworkEffect.Type.BALL_LARGE;
            else if (rt == 3) t = FireworkEffect.Type.BURST;
            else t = FireworkEffect.Type.STAR;

            Color c1 = Color.RED;
            Color c2 = Color.ORANGE;

            FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(t).trail(r.nextBoolean()).build();
            fwm.addEffect(effect);
            int rp = r.nextInt(2) + 1;
            fwm.setPower(rp);
            fw.setFireworkMeta(fwm);
            fw2.setFireworkMeta(fwm);
        }, 10L, 10);

        winner.sendMessage(ChatColor.GREEN + "Ты победил!");
        winner.sendMessage("Заработано: §e" + earnedMoney + "⭐");

        manager.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () -> { // таймер на выброс в лобби
            waitingTime--;
            for (Player p : playersList) manager.getScoreboard().updateSbInWaiting(p);

            if (waitingTime == 0) {

                Bukkit.getScheduler().cancelTask(firework);
                MinecraftServer.getServer().setMotd("RoundStartedFalse");

                for (Player p : playersList) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                    p.setGameMode(GameMode.ADVENTURE);
                    manager.getBungeeListener().connectToServer(p, "HUB");
                }

                manager.getPlugin().getServer().reload();
            }
        }, 20L, 20);
    }
}