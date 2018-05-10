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
import redis.clients.jedis.exceptions.JedisException;

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
	private PubSubListener psl;
	
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


		//connect
        if (connectPool()){

			psl = new PubSubListener();
			new Thread(psl, "UltimateChat PubSub Listener").start();

			UChat.get().getLogger().info("REDIS conected.");
        }
	}

	private class PubSubListener implements Runnable {
		private Jedis rsc;

		private PubSubListener() {
		}

		@Override
		public void run() {
			try {
				rsc = pool.getResource();
				rsc.subscribe(channel, channels);
			} catch (JedisException | ClassCastException ignored) {
			}
		}
		private void poison() {
			channel.unsubscribe();
			pool.returnResource(rsc);
		}
	}

	private boolean connectPool(){
	    if (this.pool == null || this.pool.isClosed()){
            if (auth.isEmpty()){
                this.pool = new JedisPool(poolCfg, ip, port, 0);
            } else {
                this.pool = new JedisPool(poolCfg, ip, port, 0, auth);
            }

			Jedis jedis = null;
            try {
                jedis = this.pool.getResource();
				jedis.exists(String.valueOf(System.currentTimeMillis()));
				return true;
            } catch (JedisConnectionException e){
				UChat.get().getLogger().warning("REDIS not conected! Try again with /chat reload, or check the status of your Redis server.");
				if (jedis != null)
					pool.returnBrokenResource(jedis);
				pool.destroy();
				pool = null;
				e.printStackTrace();
            } finally {
				if (jedis != null && pool != null) {
					pool.returnResource(jedis);
				}
			}
        }
        return false;
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
				connectPool();
				Jedis jedis = pool.getResource();
                try {
                    //string 0 1 2
                    jedis.publish("tellsend", this.thisId+"$"+tellReceiver+"$"+TextSerializers.JSON.serialize(text.build()));
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
					pool.returnResource(jedis);
				}
            });
		}
	}
	
	public void sendRawMessage(Text value){
		
		if (Arrays.asList(channels).contains("generic")){
			Sponge.getScheduler().createAsyncExecutor(UChat.get()).execute(() -> {
				connectPool();
				Jedis jedis = pool.getResource();
                try {
                    //string 0 1
                    jedis.publish("generic", this.thisId+"$"+TextSerializers.JSON.serialize(value));
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
					pool.returnResource(jedis);
				}
            });
		}		
	}
	
	public void sendMessage(String channel, Text value){
		
		if (Arrays.asList(channels).contains(channel)){
			Sponge.getScheduler().createAsyncExecutor(UChat.get().instance()).execute(() -> {
				connectPool();
				Jedis jedis = pool.getResource();
                try {
                    //string 0 1
                    jedis.publish(channel, this.thisId+"$"+TextSerializers.JSON.serialize(value));
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
					pool.returnResource(jedis);
				}
            });
		}		
	}

	public void closePool(){
		UChat.get().getLogger().info("Closing REDIS...");
		psl.poison();
		pool.destroy();
		UChat.get().getLogger().info("REDIS closed.");
	}	
}
