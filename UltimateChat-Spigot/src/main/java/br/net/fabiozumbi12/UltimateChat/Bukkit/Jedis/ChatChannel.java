package br.net.fabiozumbi12.UltimateChat.Bukkit.Jedis;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import redis.clients.jedis.JedisPubSub;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;

public class ChatChannel extends JedisPubSub {
	private String[] channels;
	private String thisId;

	public ChatChannel(String[] channels){
		this.channels = channels;
		this.thisId = UChat.get().getConfig().getString("jedis.server-id").replace("$", "");
	}
	
    @Override
    public void onMessage(String channel, String message) {
    	if (!UChat.get().getConfig().getBoolean("debug.messages") && message.startsWith(this.thisId)) return;
    	
    	if (Arrays.asList(channels).contains(channel)){
    		Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){
				@Override
				public void run() {
					if (channel.equals("tellresponse")){
						String[] tellresp = message.split("@");
						if (tellresp[0].equals(thisId)) return;
						if (UChat.get().getJedis().tellPlayers.containsKey(tellresp[1])){
							Player sender = Bukkit.getPlayer(UChat.get().getJedis().tellPlayers.get(tellresp[1]));
							if (sender != null && sender.isOnline()){
								if (tellresp[2].equals("false")){
									UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("listener.invalidplayer"));
								} else {
									UCUtil.performCommand(sender, Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + tellresp[3]);
								}
							}							
						}						
						return;
					}
					
					if (channel.equals("tellsend")){
						String[] msgc = message.split("\\$");
						
						String id = msgc[0];
						String tellrec = msgc[1];
						String messagef = msgc[2];
													
						Player play = Bukkit.getPlayer(tellrec);
						if (play == null){
							UChat.get().getJedis().getPool().getResource().publish("tellresponse", thisId+"@"+tellrec+"@false");
							return;
						} else {
							UChat.get().getJedis().getPool().getResource().publish("tellresponse", thisId+"@"+tellrec+"@true@"+messagef.replace("@", ""));
						}
						UChat.get().getJedis().tellPlayers.remove(tellrec);
						Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Private message from server "+id+" to player "+tellrec));
						
						//send
						UCUtil.performCommand(play, Bukkit.getConsoleSender(), "tellraw "+play.getName()+" "+messagef);
						return;
					}

					if (!channel.equals("generic")){
						String[] msgc = message.split("\\$");
						
						String id = msgc[0];
						String messagef = msgc[1];
				    	
						UCChannel ch = UChat.get().getConfig().getChannel(channel);
						if (ch.getDistance() == 0){
							if (ch.neeFocus()){
								for (CommandSender receiver:ch.getMembers()){
									UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw " + receiver.getName() + " " + messagef);
								}
							} else {
								for (Player receiver:Bukkit.getServer().getOnlinePlayers()){
									if (UCPerms.channelReadPerm(receiver, ch)){
										UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw " + receiver.getName() + " " + messagef);
									}									
								}
							}	
						}
						Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Message to channel "+ch.getName()+" from: "+id));
					} else {
						String[] msgc = message.split("\\$");
						
						String id = msgc[0];
						String messagef = msgc[1];	
						for (Player receiver:Bukkit.getServer().getOnlinePlayers()){
							UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw " + receiver.getName() + " " + messagef);							
						}
						Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Raw Message from: "+id));
					}	
				}	    		
	    	});
    	}
    }
}
