package TntTag.shop;

import TntTag.Main;
import TntTag.data.UpdateData;
import TntTag.manager.Manager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Buffs {

    private int price;
    private int speedValue;
    private int slowValue;
    private int invulValue;

    private Manager manager;
    private UpdateData updateData;
    private Inventories inventories;

    public Buffs(Manager manager) {
        this.manager = manager;
        this.updateData = manager.getUpdateData();
        this.inventories = manager.getInv();
    }

    public void buyBuff(Player p, Material material) {

        speedValue = getBuffValue(p, "Speed");
        slowValue = getBuffValue(p, "Slow");
        invulValue = getBuffValue(p, "Invul");
        int money = getMoney(p);
        int cost;

        switch (String.valueOf(material))  // Свитчим тип нажатого предмета и снимаем деньги, даем лвл бафа
        {
            case "FEATHER": {
                if (speedValue == 5) return;
                cost = getItemPrice(p, "Speed");
                if (money < cost) {
                    p.sendMessage("Недостаточно монет!");
                    inventories.createShopInv(p);
                    return;
                }
                updateData.updatePlayerMoney(p, cost, false);
                updateData.addBuffCount(p, "Speed");
                p.sendMessage(ChatColor.GREEN + "Покупка успешна!");
                break;
            }
            case "DIAMOND_SWORD": {
                if (slowValue == 5) return;
                cost = getItemPrice(p, "Slow");
                if (money < cost) {
                    p.sendMessage("Недостаточно монет!");
                    inventories.createShopInv(p);
                    return;
                }
                updateData.updatePlayerMoney(p, cost, false);
                updateData.addBuffCount(p, "Slow");
                p.sendMessage(ChatColor.GREEN + "Покупка успешна!");
                break;
            }
            case "GOLDEN_APPLE": {
                if (invulValue == 5) return;
                cost = getItemPrice(p, "Invul");
                if (money < cost) {
                    p.sendMessage("Недостаточно монет!");
                    inventories.createShopInv(p);
                    return;
                }
                updateData.updatePlayerMoney(p, cost, false);
                updateData.addBuffCount(p, "Invul");
                p.sendMessage(ChatColor.GREEN + "Покупка успешна!");
                break;
            }
        }
        inventories.createShopInv(p);
    }

    int getItemPrice(Player p, String item) {

        speedValue = getBuffValue(p, "Speed");
        slowValue = getBuffValue(p, "Slow");
        invulValue = getBuffValue(p, "Invul");

        switch(item) { // Свитчим предмет и свитчим уровень предмета, от которого зависит цена, позже вынесу в файл (если не забуду)
            case "Speed": {
                switch (speedValue) {
                    case 0:
                        price = 1000;
                        break;
                    case 1:
                        price = 2000;
                        break;
                    case 2:
                        price = 5000;
                        break;
                    case 3:
                        price = 10000;
                        break;
                    default:
                        price = 15000;
                        break;
                }
            }
            break;
            case "Slow": {
                switch (slowValue) {
                    case 0:
                        price = 2000;
                        break;
                    case 1:
                        price = 4000;
                        break;
                    case 2:
                        price = 8000;
                        break;
                    case 3:
                        price = 15000;
                        break;
                    default:
                        price = 20000;
                        break;
                }
            }
            break;
            case "Invul": {
                switch (invulValue) {
                    case 0:
                        price = 3000;
                        break;
                    case 1:
                        price = 6000;
                        break;
                    case 2:
                        price = 15000;
                        break;
                    case 3:
                        price = 20000;
                        break;
                    default:
                        price = 30000;
                        break;
                }
            }
            break;
        }
        return price;
    }

    public int getBuffValue(Player p, String type) {
        int buffValue = 5; // 5 по умолчанию, чтобы в случае ошибки оно считало, что у игрока максимальный уровень бафа и не давало купить новые
        try {
            PreparedStatement statement = Main.getInstance().getConnection().prepareStatement(
                    "SELECT * FROM users WHERE UUID = ?" // Speed, Slow, Invul
            );
            statement.setString(1, p.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            result.next();
            buffValue = result.getInt(type); // получаем из БД колонку с названием type, записываем её в buffValue и возвращаем
            statement.close();
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffValue;
    }

    private int getMoney(Player p) {
        int money = 0;
        try {
            PreparedStatement statement = Main.getInstance().getConnection().prepareStatement(
                    "SELECT Money FROM users WHERE `UUID` = ?"
            );
            statement.setString(1, p.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            result.next();
            money = result.getInt("Money");
            statement.close();
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return money;
    }

}
