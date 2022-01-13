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
import java.sql.Statement;

public class Main extends JavaPlugin {
    private Manager manager;

    private static Main instance;
    private Connection connection;

    @Override
    public void onEnable() {
        manager = new Manager(this);

        saveDefaultConfig();
        manager.getConfig().loadConfig();

        System.out.println("[TntTag] Loaded!");
        MinecraftServer.getServer().setMotd("GameStartFalse");

        setInstance(this);
        listConnection();

        Bukkit.getPluginManager().registerEvents(new Handler(manager), this);
        Bukkit.getPluginManager().registerEvents(new SubListeners(manager), this);

        getCommand("arena").setExecutor(new Commands(manager));

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
            String connectUrl = "jdbc:mysql://" + manager.getConfig().dbUrl + "/" + manager.getConfig().dbMainTable;
            this.connection = DriverManager.getConnection(connectUrl, manager.getConfig().dbUser, manager.getConfig().dbPassword);

            String createTable = "CREATE TABLE IF NOT EXISTS " + manager.getConfig().dbMainTable + "." + manager.getConfig().dbTable + " (" // подключаемся к бд и создаем таблицу
                    + "ID INT(11) NOT NULL primary key AUTO_INCREMENT,"
                    + "UUID VARCHAR(36) NOT NULL,"
                    + "Nick VARCHAR(26) NOT NULL,"
                    + "Money INT(11) NOT NULL,"
                    + "Win INT(11) NOT NULL,"
                    + "Game INT(11) NOT NULL,"
                    + "Speed INT(11) NOT NULL,"
                    + "Slow INT(11) NOT NULL,"
                    + "Invul INT(11) NOT NULL)";

            Statement statement = this.connection.createStatement();
            statement.execute(createTable);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[TntTag] Couldn`t connect to the database, disabling plugin...");
            onDisable();
        }
    }
}
