package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import br.net.fabiozumbi12.UltimateChat.Bukkit.*;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class UChatBungee implements PluginMessageListener, Listener {

    public static void sendBungee(UCChannel ch, UltimateFancy text) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(UChat.get().getUCConfig().getString("bungee.server-id"));
        out.writeUTF(ch.getAlias());
        out.writeUTF(text.toString());

        Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        try {
            p.sendPluginMessage(UChat.get(), "bungee:uchat", out.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("bungee:uchat")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        String id = "";
        String ch = "";
        String json = "";
        try {
            id = in.readUTF();
            ch = in.readUTF();
            json = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UCChannel chan = UChat.get().getChannel(ch);
        if (chan == null || !chan.isBungee()) {
            return;
        }

        if (chan.getDistance() == 0) {
            if (chan.neeFocus()) {
                for (String receiver : chan.getMembers()) {
                    if (Bukkit.getPlayer(receiver) != null) {
                        UCUtil.performCommand(Bukkit.getPlayer(receiver), Bukkit.getConsoleSender(), "tellraw " + receiver + " " + json);
                    }
                }
            } else {
                for (Player receiver : Bukkit.getServer().getOnlinePlayers()) {
                    if (UCPerms.channelReadPerm(receiver, chan)) {
                        UCUtil.performCommand(receiver, Bukkit.getConsoleSender(), "tellraw " + receiver.getName() + " " + json);
                    }
                }
            }
        }
        Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Bungee message to channel " + chan.getName() + " from: " + id));
    }
}
