package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.PostFormatChatMessageEvent;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onchatmessage(PostFormatChatMessageEvent e){
		if (e.isCancelled() || e.getChannel() == null || !e.getChannel().isBungee()){
			return;
		}
		e.setCancelled(true);
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(UChat.get().getConfig().getString("bungee.server-id"));
		out.writeUTF(e.getChannel().getAlias());
        out.writeUTF(e.getReceiverMessage(e.getSender()).toString());
        
	    Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
	    p.sendPluginMessage(UChat.get(), "uChat", out.toByteArray());
	}
}
