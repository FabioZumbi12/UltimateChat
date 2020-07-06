/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.UltimateChat.Bukkit.jedis;

import br.net.fabiozumbi12.UltimateChat.Bukkit.*;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UChatColor;
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import org.bukkit.Bukkit;
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
                pool.destroy();
                pool = null;
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public void sendTellMessage(CommandSender sender, String tellReceiver, String msg) {
        UltimateFancy fancy = new UltimateFancy(UChat.get());
        fancy.textAtStart(UChatColor.translateAlternateColorCodes(this.thisId));

        //spy
        if (!UCPerms.hasPerm(sender, "uchat.chat-spy.bypass")) {
            for (Player receiver : UChat.get().getServer().getOnlinePlayers()) {
                if (!receiver.getName().equals(tellReceiver) && !receiver.equals(sender) &&
                        UChat.get().isSpy.contains(receiver.getName()) && UCPerms.hasSpyPerm(receiver, "private")) {
                    String spyformat = UChat.get().getUCConfig().getString("general.spy-format");

                    spyformat = spyformat.replace("{output}", UChatColor.stripColor(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell")).toOldFormat()));
                    receiver.sendMessage(UChatColor.translateAlternateColorCodes(spyformat));
                }
            }
        }

        fancy.appendString(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell")).toString());
        tellPlayers.put(tellReceiver, sender.getName());

        if (Arrays.asList(channels).contains("tellsend")) {
            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                Jedis jedis = pool.getResource();
                try {
                    jedis.publish("tellsend", thisId + "$" + tellReceiver + "$" + fancy.toString());
                } catch (JedisConnectionException e) {
                    e.printStackTrace();
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
                    e.printStackTrace();
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
                    e.printStackTrace();
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
            } catch (Exception ignored) {
            }
        }
    }
}