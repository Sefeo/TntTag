package TntTag.listener;

import TntTag.manager.Manager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeListener implements PluginMessageListener {

    private Manager manager;

    public BungeeListener(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;
    }

    public void connectToServer(Player p, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        p.sendPluginMessage(manager.getPlugin(), "BungeeCord", out.toByteArray());
    }
}
