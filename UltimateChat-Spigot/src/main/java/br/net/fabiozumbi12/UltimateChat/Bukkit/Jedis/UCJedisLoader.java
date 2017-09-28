package br.net.fabiozumbi12.UltimateChat.Bukkit.Jedis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UltimateFancy;

public class UCJedisLoader {
	private JedisPool pool;
	private String[] channels;
	private ChatChannel channel;
	protected HashMap<String, String> tellPlayers = new HashMap<String, String>();
	
	protected JedisPool getPool(){
		return this.pool;
	}
	
	public UCJedisLoader(String ip, int port, String auth, List<UCChannel> channels){
		channels.add(new UCChannel("generic"));
		channels.add(new UCChannel("tellsend"));
		channels.add(new UCChannel("tellresponse"));
		
		String[] newChannels = new String[channels.size()];
		for (int i = 0; i < channels.size(); i++){
			newChannels[i] = channels.get(i).getName().toLowerCase();
		}
		
		this.channels = newChannels;
		channel = new ChatChannel(newChannels);
		
		if (auth.isEmpty()){
			this.pool = new JedisPool(ip, port);
		} else {
			this.pool = new JedisPool(new JedisPoolConfig(), ip, port, 10000, auth);
		}	    
		
		Jedis jedis = this.pool.getResource();
		new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	            	jedis.subscribe(channel, newChannels);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }).start();   
		
	    UChat.get().getUCLogger().info("JEDIS conected.");
	}	
	
	public void sendTellMessage(CommandSender sender, String tellReceiver, String msg){
		for (Player receiver:UChat.get().getServer().getOnlinePlayers()){			
			if (!receiver.equals(tellReceiver) && !receiver.equals(sender) && UChat.get().isSpy.contains(receiver.getName())){
				String spyformat = UChat.get().getConfig().getString("general.spy-format");
				
				spyformat = spyformat.replace("{output}", ChatColor.stripColor(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell"), true).toOldFormat()));					
				receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', spyformat));
			}
		}
		UltimateFancy fancy = UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell"), false);
		
		fancy.appendAtFirst("{\"tellreceiver\":\""+tellReceiver+"\"}");
		tellPlayers.put(tellReceiver, sender.getName());
		
		if (Arrays.asList(channels).contains("tellsend")){
			Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){
				@Override
				public void run() {
					try {
						Jedis jedis = pool.getResource();
						jedis.publish("tellsend", fancy.toString());
						jedis.quit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	    		
	    	});
		}
	}
	
	public void sendRawMessage(String value){	
		if (Arrays.asList(channels).contains("generic")){
			Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){
				@Override
				public void run() {
					try {
						Jedis jedis = pool.getResource();
						jedis.publish("generic", "%"+UChat.get().getConfig().getString("jedis.server-id")+"%"+value);
						jedis.quit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	    		
	    	});
		}		
	}
	
	public void sendMessage(String channel, String value){	
		if (Arrays.asList(channels).contains(channel)){
			Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){
				@Override
				public void run() {
					try {
						Jedis jedis = pool.getResource();
						jedis.publish(channel, "%"+UChat.get().getConfig().getString("jedis.server-id")+"%"+value);
						jedis.quit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	    		
	    	});
		}		
	}

	public void closePool(){
		UChat.get().getUCLogger().info("Closing JEDIS...");
		this.channel.unsubscribe();
		this.pool.destroy();
		UChat.get().getUCLogger().info("JEDIS closed.");
	}
}
