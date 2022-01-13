
package TntTag.commands;

import TntTag.manager.Manager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private Manager manager;

    public Commands(Manager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;

        switch (cmd.getName()) { // для будущих команд

            case "arena":
                if (!p.isOp()) {
                    p.sendMessage(ChatColor.RED + "У вас недостаточно прав");
                    return true;
                }
                if (manager.getPlugin().getServer().getOnlinePlayers().size() < 2) {
                    p.sendMessage("Недостаточно игроков");
                    return true;
                }
                manager.getArena().waitingTime = 5; // ставим время до старта 5 секунд
                return true;

        }
        return false;
    }
}