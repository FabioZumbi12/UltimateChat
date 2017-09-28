package br.net.fabiozumbi12.UltimateChat.Bukkit.Jedis;

import java.util.Arrays;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import redis.clients.jedis.JedisPubSub;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UltimateFancy;

public class ChatChannel extends JedisPubSub {
	private String[] channels;

	public ChatChannel(String[] channels){
		this.channels = channels;
	}
	
    @Override
    public void onMessage(String channel, final String message) {
    	if (!UChat.get().getConfig().getBoolean("debug.messages") && message.startsWith("%"+UChat.get().getConfig().getString("jedis.server-id")+"%")) return;
    	
    	if (Arrays.asList(channels).contains(channel)){
    		Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){
				@Override
				public void run() {
					if (channel.equals("tellresponse")){
						String[] tellresp = message.split("@");
						if (tellresp[0].equals(UChat.get().getConfig().getString("jedis.server-id"))) return;
						if (UChat.get().getJedis().tellPlayers.containsKey(tellresp[1])){
							Player sender = Bukkit.getPlayer(UChat.get().getJedis().tellPlayers.get(tellresp[1]));
							if (sender != null && sender.isOnline()){
								if (tellresp[2].equals("false")){
									UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("listener.invalidplayer"));
								} else {
									UltimateFancy fancy = new UltimateFancy();
									fancy.appendString(tellresp[3]);
									fancy.send(sender);
									fancy.send(Bukkit.getConsoleSender());
								}
							}							
						}						
						return;
					}
					
					if (channel.equals("tellsend")){
						JSONArray jarray = (JSONArray)JSONValue.parse(message);
						JSONObject jrec = null;
						Player play = null;
						for (Object obj:jarray){
							if (obj.toString().isEmpty()) continue;
							JSONObject json = (JSONObject) JSONValue.parse(obj.toString());
							if (json.containsKey("tellreceiver")){
								play = Bukkit.getPlayer((String)json.get("tellreceiver"));
								if (play == null){
									UChat.get().getJedis().getPool().getResource().publish("tellresponse", UChat.get().getConfig().getString("jedis.server-id")+"@"+(String)json.get("tellreceiver")+"@false");
									return;
								} else {
									UChat.get().getJedis().getPool().getResource().publish("tellresponse", UChat.get().getConfig().getString("jedis.server-id")+"@"+(String)json.get("tellreceiver")+"@true@"+message.replace("@", ""));
								}
								UChat.get().getJedis().tellPlayers.remove((String)json.get("tellreceiver"));
								jrec = json;
								break;
							}							
						}						
						UltimateFancy fancy = new UltimateFancy();
						fancy.appendString(message);
						fancy.removeObject(jrec);
						fancy.send(play);		
						fancy.send(Bukkit.getConsoleSender());	
						for (Player receiver:UChat.get().getServer().getOnlinePlayers()){			
							if (UChat.get().isSpy.contains(receiver.getName())){
								String spyformat = UChat.get().getConfig().getString("general.spy-format");								
								spyformat = spyformat.replace("{output}", ChatColor.stripColor(fancy.toOldFormat()));					
								receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', spyformat));
							}
						}
						return;
					}

					if (!channel.equals("generic")){						
						String serverid = message.substring(0, message.indexOf("["));
				    	serverid = ChatColor.translateAlternateColorCodes('&', serverid.replace("%", ""));    	
				    	UltimateFancy fancy = new UltimateFancy(serverid);    	
				    	fancy.appendString(message.substring(message.indexOf("[")));
				    	
						UCChannel ch = UChat.get().getConfig().getChannel(channel);
						if (ch.getDistance() == 0){
							if (ch.neeFocus()){
								for (CommandSender receiver:ch.getMembers()){
									UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw "+receiver.getName()+" "+fancy.toString());
								}
							} else {
								for (Player receiver:Bukkit.getServer().getOnlinePlayers()){
									if (UCPerms.channelReadPerm(receiver, ch)){
										UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw "+receiver.getName()+" "+fancy.toString());
									}									
								}
							}	
						}
						fancy.send(Bukkit.getConsoleSender());
					} else {
						UltimateFancy generic = new UltimateFancy();
						generic.appendString(message.substring(message.indexOf("[")));
						for (Player receiver:Bukkit.getServer().getOnlinePlayers()){
							UCUtil.performCommand((Player)receiver, Bukkit.getConsoleSender(), "tellraw "+receiver.getName()+" "+generic.toString());							
						}
						generic.send(Bukkit.getConsoleSender());
					}	
				}	    		
	    	});
    	}
    }
}
