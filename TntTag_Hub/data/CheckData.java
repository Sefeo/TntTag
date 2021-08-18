package TntTag_Hub.data;

import TntTag_Hub.Main;
import TntTag_Hub.manager.Manager;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckData {

    private Manager manager;

    public CheckData(Manager manager) {
        this.manager = manager;
    }

    public void checkPlayerData(Player player) { // проверка на то, есть ли игрок в БД, если нет, то записываем туда
        try {
            PreparedStatement statement = Main.getInstance().getConnection().prepareStatement(
                    "SELECT * FROM `users` WHERE `UUID` = ?"
            );
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            boolean playerHasData = result.next();
            statement.close();
            result.close();

            if (!playerHasData) {
                PreparedStatement preparedStatementOne = Main.getInstance().getConnection().prepareStatement(
                        "INSERT INTO `users`(`UUID`, `Nick`, `Money`, `Win`, `Game`, `Speed`, `Slow`, `Invul`) VALUES (?,?,?,?,?,?,?,?)"
                );
                preparedStatementOne.setString(1, player.getUniqueId().toString());
                preparedStatementOne.setString(2, player.getDisplayName());
                preparedStatementOne.setInt(3, 0);
                preparedStatementOne.setInt(4, 0);
                preparedStatementOne.setInt(5, 0);
                preparedStatementOne.setInt(6, 0);
                preparedStatementOne.setInt(7, 0);
                preparedStatementOne.setInt(8, 0);

                preparedStatementOne.executeUpdate();
                preparedStatementOne.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
