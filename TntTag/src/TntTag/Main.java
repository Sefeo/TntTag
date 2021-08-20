package TntTag;

import TntTag.listener.BungeeListener;
import TntTag.listener.SubListeners;
import TntTag.manager.Manager;
import TntTag.commands.Commands;
import TntTag.listener.Handler;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {

    private static Main instance;
    private Connection connection;

    @Override
    public void onEnable() {
        Manager manager = new Manager(this);

        MinecraftServer.getServer().setMotd("GameStartFalse");

        setInstance(this);
        listConnection();

        Bukkit.getPluginManager().registerEvents(new Handler(manager), this);
        Bukkit.getPluginManager().registerEvents(new SubListeners(manager), this);

        getCommand("arena").setExecutor(new Commands(manager));
        getCommand("sb").setExecutor(new Commands(manager));

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener(manager));
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
