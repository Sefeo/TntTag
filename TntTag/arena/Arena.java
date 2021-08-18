package TntTag.arena;

import TntTag.data.UpdateData;
import TntTag.listener.BungeeListener;
import TntTag.manager.Manager;
import TntTag.scoreboard.Scoreboard;
import TntTag.shop.Buffs;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Arena {

    public int waitingTime = 60;
    public int round;
    public int startingTimer;

    private int inGameTimer;
    private int firework;
    private int backToLobby;
    private int earnedMoney;

    private Collection<? extends Player> playersList; // вообще все игроки  на сервере, в том числе наблюдатели
    public List<Player> tntList = new ArrayList<>();
    public List<Player> playersWithoutTNT; // игроки, которые не являются ТНТ
    public List<Player> survivors; // живые в раунде

    public boolean roundStarted = false;
    public boolean isWaitingTimerOn = false; // переменная, предотвращающая двойной запуск таймеров
    public HashMap<Player, Boolean> invulBool = new HashMap<>();

    private Manager manager;
    private UpdateData updateData;
    private Scoreboard scoreboards;
    private Buffs buffs;
    private BungeeListener bungeeListener;
    private Plugin plugin;

    public Arena(Manager manager) {
        this.manager = manager;
        this.updateData = manager.getUpdateData();
        this.scoreboards = manager.getScoreboard();
        this.buffs = manager.getBuffs();
        this.bungeeListener = manager.getBungeeListener();
        this.plugin = manager.getPlugin();
    }

    public void chooseTNTs() { // выбор ТНТ при начале раунда
        Random r = new Random();
        int rt = r.nextInt(playersWithoutTNT.size());
        int tntCount = (int) Math.round(playersWithoutTNT.size() * 0.3); // Выбираем рандомных ТНТ исходя из кол-ва игроков

        for (int i = 0; i < tntCount; i++) {
            Player tnt = playersWithoutTNT.get(rt);
            tnt.getInventory().setHelmet(new ItemStack(Material.TNT));
            tnt.sendMessage(ChatColor.RED + "Передай динамит другому игроку, ударив его!");
            tnt.getInventory().setItem(0, new ItemStack(Material.TNT));
            tntList.add(tnt);
            playersWithoutTNT.remove(tnt);
        }

        for(Player p : survivors) scoreboards.updateSbInGame(p); // обновляем скорборды для всех живых

        for (Player p : playersWithoutTNT) {
            int invul = buffs.getBuffValue(p, "Invul");
            int rand = 1 + (int) (Math.random() * 100);
            if(invul >= 1 && rand < invul) // рандом неуяза, я хз как сделать лучше))
                invulBool.put(p, true); // переменная, означающая, что игрок временно неуязвим, используется в onHit
            invulBoolTimer(p);
        }
    }

    private void invulBoolTimer(Player p) { // таймер на снятие неуязвимости
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> invulBool.put(p, false), 60L);
    }

    public void waitGame() { // ожидание начала раунда, запускается в хендлере
        playersList = plugin.getServer().getOnlinePlayers();

        assert (playersList.size() != 0);

        if (playersList.size() < 24)
            waitingTime = (24-playersList.size()) * 10; // добавляем по 10 секунд ожидания за каждый пустой слот
        else waitingTime = 20;

        for (Player p : playersList) scoreboards.updateSbInWaiting(p); // обновляем скорборды для всех один раз при запуске метода

        if(isWaitingTimerOn) return; // Когда onJoin вызывает этот метод и таймер уже запущен, то обновляется только скорборд
        isWaitingTimerOn = true;

        startingTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> { // разный текст "до начала" для разного кол-ва секунд
            switch(waitingTime) {
                case 20: case 10:
                    plugin.getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунд!");
                    break;
                case 5:
                    plugin.getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунд!");
                    for (Player p : playersList) p.getInventory().clear();
                    MinecraftServer.getServer().setMotd("RoundStartedTrue");
                    break;
                case 2: case 3: case 4:
                    plugin.getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунды!");
                    break;
                case 1:
                    plugin.getServer().broadcastMessage("§aДо начала §f" + waitingTime + "§a секунда!");
                    break;
                case 0:
                    waitingTime = 60; // теперь waitingTime показывает время до взрыва ТНТ
                    for (Player p : playersList) {
                        if(!survivors.contains(p)) survivors.add(p);
                        if(!playersWithoutTNT.contains(p)) playersWithoutTNT.add(p); // добавляем всех игроков в списки
                    }
                    startRound();
                    roundStarted = true;
                    plugin.getServer().broadcastMessage("§aИгра началась!");
                    Bukkit.getScheduler().cancelTask(startingTimer);
                    round = 1;
                    chooseTNTs();
                    break;
            }
            waitingTime--;
            for (Player p : playersList) scoreboards.updateSbInWaiting(p); // обновляем скорборды для всех каждую секунду таймера
        }, 20L, 20);
    }

    private void startRound() {
        playersList = manager.getPlayers();

        inGameTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> { // ежесекундный таймер в раунде
            if (roundStarted) {
                waitingTime--; // теперь waitingTime показывает время до взрыва ТНТ, 60 секунд
                for (Player p : survivors) {
                    p.setLevel(waitingTime); // лвл отображает время до взрыва
                    scoreboards.updateSbInGame(p); // обновляем всем скорборд раунда каждую секунду
                    if (waitingTime == 0) {
                        if (tntList.contains(p)) { // взрываем всех ТНТ когда таймер доходит до нуля
                            p.getWorld().createExplosion(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 4F, false, false);
                            p.setGameMode(GameMode.SPECTATOR);
                            p.sendMessage(ChatColor.YELLOW + "Ты проиграл!");

                            earnedMoney = (round - 1) * 50; // каждый раунд по 50 монеток
                            if (earnedMoney > 0) {
                                p.sendMessage("Заработано: §e" + earnedMoney + "⭐");
                                updateData.updatePlayerMoney(p, earnedMoney, true);
                            }

                            p.getInventory().clear();
                            p.removePotionEffect(PotionEffectType.SPEED);

                            tntList.remove(p);
                            playersWithoutTNT.remove(p);
                            survivors.remove(p); // удаляем проигравшего из всех списков

                            updateData.updateWinCount(p, false);
                            scoreboards.updateSbNoPlayers(p); // показываем проигравшему скорборд хаба

                            if (survivors.size() > 1) {
                                startNextRound();
                                Bukkit.getScheduler().cancelTask(inGameTimer);
                            } else playerWin(survivors.get(0)); // если выживших 2+, то новый раунд, если 1 - выбираем победителя
                        }
                    }
                }
            }
        }, 20L, 20);
    }

    private void startNextRound() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> { // ждём 5с перед началом нового раунда
            round++;
            waitingTime = 60 - ((round - 1) * 5); // с каждым раундом время до взрыва уменьшается на 5с
            for (Player p : survivors) scoreboards.updateSbInGame(p);
            startRound();
            chooseTNTs();
            if (survivors.size() < 7) { // когда выживших 6 и меньше, то тпаем их всех в центр карты
                for (Player p : survivors) {
                    Location loc = new Location(p.getWorld(), 1, 29, 1);
                    p.teleport(loc);
                }
            }
        }, 100L);
    }

    public void playerWin(Player winner) {
        roundStarted = false;

        winner.getInventory().clear();
        winner.setLevel(0);
        winner.setAllowFlight(true);
        winner.setFlying(true);

        updateData.updateWinCount(winner, true); // добавляет одну победу в БД
        earnedMoney = round * 50 + 100; // по 50 монет за каждый раунд, 100 за саму победу
        updateData.updatePlayerMoney(winner, earnedMoney, true);
        round = 1;

        waitingTime = 8; // 8 секунд до выброса в лобби
        for (Player p : playersList) scoreboards.updateSbInWaiting(p); // устанавливаем скорборд ожидания
        isWaitingTimerOn = false; // сбрасываем защиту от двойного таймера

        firework = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> { // фейверки победителю :D

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

        backToLobby = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            waitingTime--;
            for (Player p : playersList)scoreboards.updateSbInWaiting(p); // обновляем скорборд каждую секунду

            if (waitingTime == 0) {

                Bukkit.getScheduler().cancelTask(firework);
                MinecraftServer.getServer().setMotd("RoundStartedFalse");

                for (Player p : playersList) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                    p.setGameMode(GameMode.ADVENTURE);
                    bungeeListener.connectToServer(p, "HUB");
                }

                Bukkit.getScheduler().cancelTask(backToLobby);
                plugin.getServer().reload();
            }
        }, 20L, 20);
    }
}