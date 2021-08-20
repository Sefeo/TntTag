package TntTag_Hub.manager;

import TntTag_Hub.Main;
import TntTag_Hub.data.CheckData;
import TntTag_Hub.data.UpdateData;
import TntTag_Hub.items.ServerJoinItem;
import TntTag_Hub.items.ShopItem;
import TntTag_Hub.listener.BungeeListener;
import TntTag_Hub.listener.Handler;
import TntTag_Hub.scoreboard.Scoreboard;
import TntTag_Hub.shop.Buffs;
import TntTag_Hub.shop.Inventories;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Manager {

    private Main plugin;

    private CheckData checkData;
    private UpdateData updateData;
    private Buffs buffs;
    private Inventories inv;
    private Handler handler;
    private Scoreboard scoreboard;
    private BungeeListener bungeeListener;
    private ServerJoinItem joinItem;
    private ShopItem shopItem;


    public Manager(Main main) {
        this.plugin = main;
        this.checkData = new CheckData(this);
        this.updateData = new UpdateData(this);
        this.buffs = new Buffs(this);
        this.inv = new Inventories(this);
        this.handler = new Handler(this);
        this.scoreboard = new Scoreboard(this);
        this.bungeeListener = new BungeeListener(this);
        this.joinItem = new ServerJoinItem(this);
        this.shopItem = new ShopItem(this);
    }

    public Collection<? extends Player> getPlayers() { // геттер всех игроков онлайн
        return plugin.getServer().getOnlinePlayers();
    }

    public Server getServer() { // геттер сервера
        return plugin.getServer();
    }

    public CheckData getCheckData() {
        return checkData;
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

    public ServerJoinItem getJoinItem() {
        return joinItem;
    }

    public ShopItem getShopItem() {
        return shopItem;
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
