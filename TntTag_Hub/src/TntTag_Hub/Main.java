package TntTag_Hub;

import TntTag_Hub.listener.BungeeListener;
import TntTag_Hub.listener.Handler;
import TntTag_Hub.listener.SubListeners;
import TntTag_Hub.manager.Manager;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {

    // ЭТО ОБНОВЛЁННЫЙ ХАБ, СТАРАЯ ВЕРСИЯ - TntTagHub

    private static Main instance;
    private Connection connection;

    private ArmorStand npc;

    @Override
    public void onEnable() {
        System.out.println("[TntTag_Hub] TntTag_Hub loaded! Dev: Sefeo");
        Manager manager = new Manager(this);

        setInstance(this);
        listConnection();

        Bukkit.getPluginManager().registerEvents(new Handler(manager), this);
        Bukkit.getPluginManager().registerEvents(new SubListeners(manager), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener(manager));

        World world = this.getServer().getWorlds().get(0); // создание нпс
        Location loc = new Location(world, 214, 71, 67);
        npc = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        npc.setHelmet(new ItemStack(Material.TNT));
        npc.setCustomName(ChatColor.RED + "TNT TAG");
        npc.setCustomNameVisible(true);

        int removedEntities = 0;
        for(Entity ent : world.getEntities()) {
            if(ent instanceof Player || ent instanceof ArmorStand) continue; // удаляем всех энтити кроме игроков и арморстендов
            ent.remove();
            removedEntities++;
        }
        if(removedEntities > 0) System.out.println("Entities deleted: " + removedEntities);
    }

    public void onDisable() {
        npc.remove();
    }

    public static Main getInstance() {
        return instance;
    }

    private static void setInstance(Main instance) {
        Main.instance = instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void listConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url1 = "jdbc:mysql://127.0.0.1:3306/server?user=root&password=";
            this.connection = DriverManager.getConnection(url1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
