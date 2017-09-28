package br.net.fabiozumbi12.UltimateChat.Sponge.Jedis;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;

public class UCJedisLoader {
	private JedisPool pool;
	private String[] channels;
	private ChatChannel channel;
	
	public UCJedisLoader(String ip, int port, String auth, List<UCChannel> channels){
		if (pool != null){
			closePool();
		}
		
		String[] newChannels = new String[channels.size()];
		for (int i = 0; i < channels.size(); i++){
			newChannels[i] = channels.get(i).getName().toLowerCase();
		}
		this.channels = newChannels;
		channel = new ChatChannel();
		
	    this.pool = new JedisPool(new JedisPoolConfig(), ip, port, 10000, auth);
	    this.pool.getResource().subscribe(channel, newChannels);	    
	}	
		
	public void sendMessage(String channel, String value){	
		if (Arrays.asList(channels).contains(channel)){
			Sponge.getScheduler().createAsyncExecutor(UChat.get().instance()).execute(new Runnable(){
				@Override
				public void run() {
					try {
						Jedis jedis = pool.getResource();
						jedis.publish(channel, value);
						UChat.get().getLogger().warning("Publish: "+value);
						jedis.quit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	    		
	    	});
		}		
	}

	public void closePool(){
		this.channel.unsubscribe();
		this.pool.destroy();
	}	
	
	public class ChatChannel extends JedisPubSub {
	    @Override
	    public void onMessage(String channel, String message) {
	    	if (Arrays.asList(channels).contains(channel)){
	    		Sponge.getScheduler().createAsyncExecutor(UChat.get().instance()).execute(new Runnable(){
					@Override
					public void run() {
						UCChannel ch = UChat.get().getConfig().getChannel(channel);
						if (ch.neeFocus()){
							for (CommandSource receiver:ch.getMembers()){
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+receiver.getName()+" "+message);
							}
						} else {
							for (Player receiver:Sponge.getServer().getOnlinePlayers()){
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+receiver.getName()+" "+message);
							}
						}						
					}	    		
		    	});
	    	}
	    }
	}
}
