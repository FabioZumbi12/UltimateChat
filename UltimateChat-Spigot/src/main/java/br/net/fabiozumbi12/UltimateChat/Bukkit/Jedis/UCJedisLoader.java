package br.net.fabiozumbi12.UltimateChat.Bukkit.Jedis;

import br.net.fabiozumbi12.UltimateChat.Bukkit.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UCJedisLoader {
    protected HashMap<String, String> tellPlayers = new HashMap<>();
    private JedisPool pool;
    private String[] channels;
    private ChatChannel channel;
    private String thisId;
    private String ip;
    private int port;
    private String auth;
    private JedisPoolConfig poolCfg;
    private PubSubListener psl;

    public UCJedisLoader(String ip, int port, String auth, List<UCChannel> channels) {
        this.thisId = UChat.get().getUCConfig().getString("jedis.server-id").replace("$", "");
        this.ip = ip;
        this.port = port;
        this.auth = auth;

        channels.add(new UCChannel("generic"));
        channels.add(new UCChannel("tellsend"));
        channels.add(new UCChannel("tellresponse"));

        String[] newChannels = new String[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            newChannels[i] = channels.get(i).getName().toLowerCase();
        }

        this.channels = newChannels;
        channel = new ChatChannel(newChannels);
        poolCfg = new JedisPoolConfig();

        //connect
        if (connectPool()) {

            psl = new PubSubListener();
            new Thread(psl, "UltimateChat PubSub Listener").start();

            UChat.get().getUCLogger().info("REDIS conected.");
        }
    }

    protected JedisPool getPool() {
        return this.pool;
    }

    private boolean connectPool() {
        if (this.pool == null || this.pool.isClosed()) {
            if (auth.isEmpty()) {
                this.pool = new JedisPool(poolCfg, ip, port, 0);
            } else {
                this.pool = new JedisPool(poolCfg, ip, port, 0, auth);
            }

            Jedis jedis = null;
            try {
                jedis = this.pool.getResource();
                jedis.exists(String.valueOf(System.currentTimeMillis()));
                return true;
            } catch (JedisConnectionException e) {
                UChat.get().getUCLogger().warning("REDIS not conected! Try again with /chat reload, or check the status of your Redis server.");
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
            return false;
        }
        return true;
    }

    public void sendTellMessage(CommandSender sender, String tellReceiver, String msg) {
        UltimateFancy fancy = new UltimateFancy();
        fancy.textAtStart(ChatColor.translateAlternateColorCodes('&', this.thisId));

        //spy
        if (!UCPerms.hasPermission(sender, "uchat.chat-spy.bypass")) {
            for (Player receiver : UChat.get().getServer().getOnlinePlayers()) {
                if (!receiver.getName().equals(tellReceiver) && !receiver.equals(sender) &&
                        UChat.get().isSpy.contains(receiver.getName()) && UCPerms.hasSpyPerm(receiver, "private")) {
                    String spyformat = UChat.get().getUCConfig().getString("general.spy-format");

                    spyformat = spyformat.replace("{output}", ChatColor.stripColor(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell"), true).toOldFormat()));
                    receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', spyformat));
                }
            }
        }

        fancy.appendString(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell"), false).toString());
        tellPlayers.put(tellReceiver, sender.getName());

        if (Arrays.asList(channels).contains("tellsend")) {
            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                Jedis jedis = pool.getResource();
                try {
                    jedis.publish("tellsend", thisId + "$" + tellReceiver + "$" + fancy.toString());
                } catch (JedisConnectionException e) {
                    pool.returnBrokenResource(jedis);
                    e.printStackTrace();
                } finally {
                    pool.returnResource(jedis);
                }
            });
        }
    }

    public void sendRawMessage(UltimateFancy value) {
        if (Arrays.asList(channels).contains("generic")) {
            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                Jedis jedis = pool.getResource();
                try {
                    jedis.publish("generic", thisId + "$" + value.toString());
                } catch (JedisConnectionException e) {
                    pool.returnBrokenResource(jedis);
                    e.printStackTrace();
                } finally {
                    pool.returnResource(jedis);
                }
            });
        }
    }

    public void sendMessage(String channel, UltimateFancy value) {

        if (Arrays.asList(channels).contains(channel)) {
            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                Jedis jedis = pool.getResource();
                try {
                    jedis.publish(channel, thisId + "$" + value.toString());
                } catch (JedisConnectionException e) {
                    pool.returnBrokenResource(jedis);
                    e.printStackTrace();
                } finally {
                    pool.returnResource(jedis);
                }
            });
        }
    }

    public void closePool() {
        UChat.get().getUCLogger().info("Closing REDIS...");
        if (psl != null) psl.poison();
        if (pool != null) pool.destroy();
        UChat.get().getUCLogger().info("REDIS closed.");
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
            try {
                channel.unsubscribe();
                pool.returnResource(rsc);
            } catch (Exception ignored) {
            }
        }
    }
}
