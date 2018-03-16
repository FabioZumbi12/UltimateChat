package br.net.fabiozumbi12.UltimateChat.Sponge.Jedis;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.serializer.TextSerializers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UCJedisLoader {
	private JedisPool pool;
	private String[] channels;
	private ChatChannel channel;
	protected HashMap<String, String> tellPlayers = new HashMap<>();
	private String thisId;
	private String ip;
    private int port;
    private String auth;
    private JedisPoolConfig poolCfg;
	
	protected JedisPool getPool(){
		return this.pool;
	}
	
	public UCJedisLoader(String ip, int port, String auth, List<UCChannel> channels){
		this.thisId = UChat.get().getConfig().root().jedis.server_id.replace("$", "");
		this.ip = ip;
		this.port = port;
		this.auth = auth;

		channels.add(new UCChannel("generic"));
		channels.add(new UCChannel("tellsend"));
		channels.add(new UCChannel("tellresponse"));
		
		String[] newChannels = new String[channels.size()];
		for (int i = 0; i < channels.size(); i++){
			newChannels[i] = channels.get(i).getName().toLowerCase();
		}
		
		this.channels = newChannels;
		channel = new ChatChannel(newChannels);
		poolCfg = new JedisPoolConfig();
		poolCfg.setTestOnBorrow(true);

		//connect
        if (!connectPool()){
            return;
        }

		UChat.get().getLogger().info("REDIS conected.");
	}	

	private boolean connectPool(){
	    if (this.pool == null || this.pool.isClosed()){
            if (auth.isEmpty()){
                this.pool = new JedisPool(poolCfg, ip, port, 0);
            } else {
                this.pool = new JedisPool(poolCfg, ip, port, 0, auth);
            }

            try {
                Jedis jedis = this.pool.getResource();
                jedis.configSet("timeout", "0");
                new Thread(() -> {
                    try {
                        jedis.subscribe(channel, channels);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (JedisConnectionException e){
                e.printStackTrace();
                UChat.get().getLogger().warning("REDIS not conected! Try again with /chat reload, or check the status of your Redis server.");
                return false;
            }
        }
        return true;
    }

	public void sendTellMessage(CommandSource sender, String tellReceiver, Text msg){
		Builder text = Text.builder();
		text.append(UCUtil.toText(this.thisId));
				
		//send spy
		if (!sender.hasPermission("uchat.chat-spy.bypass")){
			for (Player receiver:Sponge.getServer().getOnlinePlayers()){			
				if (!receiver.getName().equals(tellReceiver) && !receiver.equals(sender) && 
						UChat.get().isSpy.contains(receiver.getName()) && UChat.get().getPerms().hasSpyPerm(receiver, "private")){
					String spyformat = UChat.get().getConfig().root().general.spy_format;
					
					spyformat = spyformat.replace("{output}", UCUtil.stripColor(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell"), true).toPlain()));					
					receiver.sendMessage(UCUtil.toText(spyformat));					
				}
			}
		}
		
		text.append(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell"), false));			
		tellPlayers.put(tellReceiver, sender.getName());
		
		if (Arrays.asList(channels).contains("tellsend")){
			Sponge.getScheduler().createAsyncExecutor(UChat.get()).execute(() -> {
                try {
                    connectPool();
                    Jedis jedis = pool.getResource();
                    //string 0 1 2
                    jedis.publish("tellsend", this.thisId+"$"+tellReceiver+"$"+TextSerializers.JSON.serialize(text.build()));
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
		}
	}
	
	public void sendRawMessage(Text value){
		
		if (Arrays.asList(channels).contains("generic")){
			Sponge.getScheduler().createAsyncExecutor(UChat.get()).execute(() -> {
                try {
                    connectPool();
                    Jedis jedis = pool.getResource();
                    //string 0 1
                    jedis.publish("generic", this.thisId+"$"+TextSerializers.JSON.serialize(value));
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
		}		
	}
	
	public void sendMessage(String channel, Text value){
		
		if (Arrays.asList(channels).contains(channel)){
			Sponge.getScheduler().createAsyncExecutor(UChat.get().instance()).execute(() -> {
                try {
                    connectPool();
                    Jedis jedis = pool.getResource();
                    //string 0 1
                    jedis.publish(channel, this.thisId+"$"+TextSerializers.JSON.serialize(value));
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
		}		
	}

	public void closePool(){
		UChat.get().getLogger().info("Closing REDIS...");
		if (this.channel.isSubscribed()){
			this.channel.unsubscribe();
		}
		try {
			this.pool.getResource().close();
		} catch (Exception ignored){}
        this.pool.close();
		this.pool.destroy();
		UChat.get().getLogger().info("REDIS closed.");
	}	
}
