package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import br.net.fabiozumbi12.UltimateChat.Bukkit.*;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.PostFormatChatMessageEvent;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class UChatBungee implements PluginMessageListener, Listener {
	
	@Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("uChat")){
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
		if (chan == null || !chan.isBungee()){
			return;
		}
		
		if (chan.getDistance() == 0){
			if (chan.neeFocus()){
				for (CommandSender receiver:chan.getMembers()){
					UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw " + receiver.getName() + " " + json);
				}
			} else {
				for (Player receiver:Bukkit.getServer().getOnlinePlayers()){
					if (UCPerms.channelReadPerm(receiver, chan)){
						UCUtil.performCommand(receiver, Bukkit.getConsoleSender(), "tellraw " + receiver.getName() + " " + json);
					}									
				}
			}	
		}
		Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Bungee message to channel "+chan.getName()+" from: "+id));		
	}	

	public static void sendBungee(UCChannel ch, UltimateFancy text){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(UChat.get().getConfig().getString("bungee.server-id"));
        out.writeUTF(ch.getAlias());
        out.writeUTF(text.toString());

        Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        p.sendPluginMessage(UChat.get(), "uChat", out.toByteArray());
	}
}
