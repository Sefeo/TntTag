package TntTag.data;

import TntTag.manager.Manager;
import org.bukkit.entity.Player;
import TntTag.Main;

import java.sql.PreparedStatement;

public class UpdateData {

    private Manager manager;

    public UpdateData(Manager manager) {
        this.manager = manager;
    }

    public void updateWinCount(Player p, boolean isWin) { // isWin true - добавляем победу, false - убираем
        try {
            PreparedStatement preparedStatement;
            if (isWin) {
                preparedStatement = Main.getInstance().getConnection().prepareStatement(
                        "UPDATE users SET Game=Game+1, Win=Win+1 WHERE UUID = ?"
                );
            } else {
                preparedStatement = Main.getInstance().getConnection().prepareStatement(
                        "UPDATE users SET Game=Game+1, Win=Win-1 WHERE UUID = ?"
                );
            }

            preparedStatement.setString(1, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerMoney(Player p, int money, boolean isAdding) { // isAdding = true - добавить деньги, false - снять
        try {
            PreparedStatement preparedStatement;
            if(isAdding) {
                preparedStatement = Main.getInstance().getConnection().prepareStatement(
                        "UPDATE users SET Money=Money+? WHERE UUID = ?"
                );
            } else {
                preparedStatement = Main.getInstance().getConnection().prepareStatement(
                        "UPDATE users SET Money=Money-? WHERE UUID = ?"
                );
            }
            preparedStatement.setInt(1, money);
            preparedStatement.setString(2, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addBuffCount(Player p, String type) {
        try {
            PreparedStatement preparedStatement = Main.getInstance().getConnection().prepareStatement(
                    "UPDATE users SET " + type + "= " + type + "+1 WHERE UUID = ?" // КОМУ НУЖНЫ SETSTRING КОГДА ЕСТЬ КОНКАТИНАЦИЯ?!!?!
            );
            preparedStatement.setString(1, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}