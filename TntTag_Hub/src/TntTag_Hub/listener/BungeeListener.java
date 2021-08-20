package TntTag_Hub.listener;

import TntTag_Hub.items.ServerJoinItem;
import TntTag_Hub.manager.Manager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BungeeListener implements PluginMessageListener {

    private Manager manager;
    private ServerJoinItem joinItem;

    private String ip = "localhost"; // если у серверов разные айпи, то Ф тебе, иди переписывай код
    private int port = 25564; // тут надо указать максимальный порт всех серверов +1, например у меня макс. 25563, указываю 25564.

    public BungeeListener(Manager manager) {
        this.manager = manager;
        this.joinItem = manager.getJoinItem();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("BungeeCord")) return;

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String command = in.readUTF();

            if (command.equals("GetServers")) {
                String servers = in.readUTF(); // все сервера идут одним стрингом (в том числе которые просто прописаны в бандже и сейчас оффлайн)
                joinItem.serverName = servers.split(", "); // разделяем их через запятую и записываем в массив

                for (int i = 0; i < joinItem.serverName.length; i++) { // проходимся по каждому серверу
                    if (joinItem.serverName[i].equals("HUB")) continue; // кроме хаба
                    if(joinItem.serverName[i].length() == 0) continue; // и кроме пустых (если такие будут)

                    int currentPort = port - i; // порт сервера, инфу про который мы собираемся обновлять
                    try {
                        Socket s = new Socket();
                        s.connect(new InetSocketAddress(ip, currentPort), 10);

                        getCount(player, joinItem.serverName[i]);
                        joinItem.isOnline.put(joinItem.serverName[i], true); // записываем кол-во игроков и онлайн

                        String motd = getMOTD(ip, currentPort);
                        if (motd.equals("\u0012RoundStartedTrue")) joinItem.roundStarted.put(joinItem.serverName[i], true);
                        else joinItem.roundStarted.put(joinItem.serverName[i], false); // в зависимости от МОТД обозначаем идёт ли там раунд

                        s.close();
                    } catch (IOException e) {
                        joinItem.isOnline.put(joinItem.serverName[i], false); // если ексепшин, значит сервер оффлайн
                    }
                }
            }
            if (command.equals("PlayerCount")) {
                String server = in.readUTF();
                int players = in.readInt();
                joinItem.playersCount.put(server, players); // просто записываем кол-во игроков в переменную
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateServersInfo() { // запрос к бандже на получение списка серверов, в его обработке выше идёт сохранение данных о всех серверах
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("GetServers");
        } catch(Exception e) {
            e.printStackTrace();
        }
        Bukkit.getServer().sendPluginMessage(manager.getPlugin(), "BungeeCord", b.toByteArray());
    }

    void connectToServer(Player p, String server) { // подключение игрока к другому серверу
        ByteArrayDataOutput in = ByteStreams.newDataOutput();
        in.writeUTF("Connect");
        in.writeUTF(server);
        p.sendPluginMessage(manager.getPlugin(), "BungeeCord", in.toByteArray());
    }

    private void getCount(Player player, String server) { // запрос к бандже на получение кол-ва игроков

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(server);

        player.sendPluginMessage(manager.getPlugin(), "BungeeCord", out.toByteArray());

    }

    private String getMOTD(String ip, int port) { // получает мотд сервера, принцип работы сложный и нет смысла его описывать
        String motd;
        try {
            Socket socket = new Socket();
            socket.setSoTimeout(100);
            socket.connect(new InetSocketAddress(ip, port), 100);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.write(0xFE);

            int b;
            StringBuilder str = new StringBuilder();
            while ((b = in.read()) != -1) {
                if (b > 16 && b != 255 && b != 23 && b != 24) {
                    str.append((char) b);
                }
            }

            String[] data = str.toString().split("§");

            motd = data[0];

            socket.close();

        } catch (IOException e) {
            motd = "OFFLINE";
        }
        return motd;
    }

}
