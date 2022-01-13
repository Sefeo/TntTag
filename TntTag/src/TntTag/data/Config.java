package TntTag.data;

import TntTag.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class Config {
    private Manager manager;
    private FileConfiguration config;

    public String dbUrl;
    public String dbUser;
    public String dbPassword;
    public String dbTable;
    public String dbMainTable;

    public Location loc = new Location(Bukkit.getServer().getWorld("world"), 1, 29, 1);
    public HashMap<Integer, Integer> speedBuffCost = new HashMap<>();
    public HashMap<Integer, Integer> slowBuffCost = new HashMap<>();
    public HashMap<Integer, Integer> invulBuffCost = new HashMap<>();

    public HashMap<Integer, Integer> speedBuffChance = new HashMap<>();
    public HashMap<Integer, Integer> slowBuffChance = new HashMap<>();
    public HashMap<Integer, Integer> invulBuffChance = new HashMap<>();

    public Config(Manager manager) {
        this.manager = manager;
        this.config = manager.getPlugin().getConfig();
    }

    public void loadConfig() {
        dbUrl = config.getString("Database.Url"); // инфа о БД
        dbUser = config.getString("Database.User");
        dbPassword = config.getString("Database.Password");
        dbTable = config.getString("Database.Table");
        dbMainTable = config.getString("Database.MainTable");

        String locationString = config.getString("Location");
        String[] locationArray = locationString.split(", ");
        World world = Bukkit.getServer().getWorld(locationArray[0]);
        double x = Double.valueOf(locationArray[1]);
        double y = Double.valueOf(locationArray[2]);
        double z = Double.valueOf(locationArray[3]);
        loc = new Location(world, x,y,z);

        for(int i = 1; i <= 5; i++) {
            speedBuffCost.put(i, config.getInt("Speed.Level." + i + ".Cost"));
            speedBuffChance.put(i, config.getInt("Speed.Level." + i + ".Chance"));

            slowBuffCost.put(i, config.getInt("Slow.Level." + i + ".Cost"));
            slowBuffChance.put(i, config.getInt("Slow.Level." + i + ".Chance"));

            invulBuffCost.put(i, config.getInt("Invul.Level." + i + ".Cost"));
            invulBuffChance.put(i, config.getInt("Invul.Level." + i + ".Chance"));
        }
    }
}
