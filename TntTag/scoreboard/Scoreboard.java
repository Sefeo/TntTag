package TntTag.scoreboard;

import TntTag.arena.Arena;
import TntTag.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import TntTag.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Scoreboard {

    private Manager manager;
    private Arena arena;

    public Scoreboard(Manager manager) {
        this.manager = manager;
        this.arena = manager.getArena();
    }

    public void updateSbNoPlayers(Player p) { // скорборд, когда нет отсчёта до начала раунда (игрок один на сервере или в хабе)
        try {
            PreparedStatement statement = Main.getInstance().getConnection().prepareStatement(
                    "SELECT Money, Win, Game FROM users WHERE UUID = ?"
            );
            statement.setString(1, p.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            result.next();
            int coins = result.getInt("Money");
            int wins = result.getInt("Win");
            int games = result.getInt("Game");
            statement.close();
            result.close();

            ScoreboardManager managerSb = Bukkit.getScoreboardManager(); // это и всё ниже - создание самого скорборда и его полей
            final org.bukkit.scoreboard.Scoreboard board = managerSb.getNewScoreboard();
            final Objective objective = board.registerNewObjective("1", "2");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.BOLD + "§eTNT TAG");

            Score score = objective.getScore("§1");
            score.setScore(9);

            Score score1 = objective.getScore(" Игроков: " + ChatColor.GREEN  + manager.getPlayers().size() + "/24  ");
            score1.setScore(8);

            Score score2 = objective.getScore("§2");
            score2.setScore(7);

            Score score3 = objective.getScore(" Карта: §e" + "Название  ");
            score3.setScore(6);

            Score score4 = objective.getScore("§3");
            score4.setScore(5);

            Score score5 = objective.getScore(" Баланс: §e" + coins + "⭐");
            score5.setScore(4);

            Score score6 = objective.getScore(" Побед: §e" + wins);
            score6.setScore(3);

            Score score7 = objective.getScore(" Игр: §e" + games);
            score7.setScore(2);

            Score score8 = objective.getScore("§4");
            score8.setScore(1);

            p.setScoreboard(board);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String placeZeroIfNeeded(int number) { // вспомогательный метод для метода ниже
        return (number >= 10) ? Integer.toString(number) : String.format("0%s", Integer.toString(number));
    }

    private String secondsToString(int time) { // переводит секунды в формат "минут:секунд"
        int min = time / 60;
        int sec = time - (min * 60);

        String strMin = placeZeroIfNeeded(min);
        String strSec = placeZeroIfNeeded(sec);
        return String.format("%s:%s", strMin, strSec);
    }

    public void updateSbInWaiting(Player p) { // скорборд, когда идёт отсчёт до начала раунда (2+ игроков на сервере)
        try {
            PreparedStatement statement = Main.getInstance().getConnection().prepareStatement(
                    "SELECT Money, Win, Game FROM users WHERE UUID = ?"
            );
            statement.setString(1, p.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            result.next();
            int coins = result.getInt("Money");
            int wins = result.getInt("Win");
            int games = result.getInt("Game");
            statement.close();
            result.close();

            ScoreboardManager managerSb = Bukkit.getScoreboardManager(); // это и всё ниже - создание самого скорборда и его полей
            final org.bukkit.scoreboard.Scoreboard board = managerSb.getNewScoreboard();
            final Objective objective = board.registerNewObjective("1", "2");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.BOLD + "§eTNT TAG");

            Score score = objective.getScore("§1");
            score.setScore(11);

            Score score1 = objective.getScore(" Игроков: " + ChatColor.GREEN + manager.getPlayers().size() + "/24 ");
            score1.setScore(10);

            Score score2 = objective.getScore("§2");
            score2.setScore(9);

            Score score3 = objective.getScore(" Карта: §e" + "Название ");
            score3.setScore(8);

            Score score4 = objective.getScore("§3");
            score4.setScore(7);

            Score score5 = objective.getScore(" Ожидаем: §e" + secondsToString(arena.waitingTime));
            score5.setScore(6);

            Score score6 = objective.getScore("§4");
            score6.setScore(5);

            Score score7 = objective.getScore(" Баланс: §e" + coins + "⭐");
            score7.setScore(4);

            Score score8 = objective.getScore(" Побед: §e" + wins);
            score8.setScore(3);

            Score score9 = objective.getScore(" Игр: §e" + games);
            score9.setScore(2);

            Score score10 = objective.getScore("§5");
            score10.setScore(1);

            p.setScoreboard(board);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSbInGame(Player p) { // скорборд в раунде
        ScoreboardManager managerSb = Bukkit.getScoreboardManager();
        final org.bukkit.scoreboard.Scoreboard board = managerSb.getNewScoreboard();
        final Objective objective = board.registerNewObjective("1", "2");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.BOLD + "§eTNT TAG");

        Score score = objective.getScore(ChatColor.GRAY + " Раунд #" + arena.round);
        score.setScore(8);

        Score score1 = objective.getScore("§1");
        score1.setScore(7);

        Score score2;
        if (arena.waitingTime <= 15 && arena.waitingTime > 5)
            score2 = objective.getScore(ChatColor.YELLOW + " До взрыва " + ChatColor.GOLD + secondsToString(arena.waitingTime));
        else if (arena.waitingTime <= 5)
            score2 = objective.getScore(ChatColor.YELLOW + " До взрыва " + ChatColor.RED + secondsToString(arena.waitingTime));
        else
            score2 = objective.getScore(ChatColor.YELLOW + " До взрыва " + ChatColor.GREEN + secondsToString(arena.waitingTime));
        score2.setScore(6);

        Score score3 = objective.getScore("§2");
        score3.setScore(5);

        Score score4;
        if (!arena.tntList.contains(p))
            score4 = objective.getScore(" Цель: " + ChatColor.GREEN + "Убегай!  ");
        else score4 = objective.getScore(" Цель: " + ChatColor.RED + "Передай!  ");
        score4.setScore(4);

        Score score5 = objective.getScore("§3");
        score5.setScore(3);

        // А тут уже берём список выживших
        Score score6 = objective.getScore(" Осталось: " + ChatColor.GREEN + arena.survivors.size() + " Игроков");
        score6.setScore(2);

        Score score7 = objective.getScore("§4");
        score7.setScore(1);

        p.setScoreboard(board);
    }
}