package TntTag.manager;

import TntTag.arena.Arena;
import TntTag.listener.BungeeListener;
import TntTag.data.UpdateData;
import TntTag.Main;
import TntTag.shop.Buffs;
import TntTag.shop.Inventories;
import TntTag.listener.Handler;
import TntTag.scoreboard.Scoreboard;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Manager {

    private Main plugin;

    private Arena arena;
    private UpdateData updateData;
    private Buffs buffs;
    private Inventories inv;
    private Handler handler;
    private Scoreboard scoreboard;
    private BungeeListener bungeeListener;

    public Manager(Main main) {
        this.plugin = main;
        this.arena = new Arena(this);
        this.updateData = new UpdateData(this);
        this.buffs = new Buffs(this);
        this.inv = new Inventories(this);
        this.handler = new Handler(this);
        this.scoreboard = new Scoreboard(this);
        this.bungeeListener = new BungeeListener(this);
    }

    public Collection<? extends Player> getPlayers() { // геттер всех игроков онлайн
        return plugin.getServer().getOnlinePlayers();
    }

    public Server getServer() { // геттер сервера
        return plugin.getServer();
    }

    public Arena getArena() {
        return arena;
    }

    public UpdateData getUpdateData() {
        return updateData;
    }

    public Buffs getBuffs() {
        return buffs;
    }

    public Inventories getInv() {
        return inv;
    }

    public Handler getHandler() {
        return handler;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public BungeeListener getBungeeListener() {
        return bungeeListener;
    }

    public FileConfiguration getConfig(){
        return plugin.getConfig();
    }

    public void saveConfig(){
        plugin.saveConfig();
    }

    public Main getPlugin(){
        return plugin;
    }
}
