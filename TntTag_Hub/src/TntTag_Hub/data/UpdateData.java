package TntTag_Hub.data;

import TntTag_Hub.Main;
import TntTag_Hub.manager.Manager;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;

public class UpdateData {

    private Manager manager;

    public UpdateData(Manager manager) {
        this.manager = manager;
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
