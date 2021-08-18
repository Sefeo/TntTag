
package TntTag.commands;

import TntTag.arena.Arena;
import TntTag.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class Commands implements CommandExecutor {

    private Manager manager;
    private Arena arena;
    private Plugin plugin;

    public Commands(Manager manager) {
        this.manager = manager;
        this.arena = manager.getArena();
        this.plugin = manager.getPlugin();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;

        switch (cmd.getName()) {

            case "arena": {
                if (!p.isOp()) {
                    p.sendMessage(ChatColor.RED + "У вас недостаточно прав");
                    return true;
                }
                if (plugin.getServer().getOnlinePlayers().size() < 2) {
                    p.sendMessage("Недостаточно игроков");
                    return true;
                }
                arena.waitingTime = 5; // ставим время до старта 5 секунд
                return true;
            }

            case "motd": return true;
        }
        return false;
    }
}