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

package br.net.fabiozumbi12.UltimateChat.Sponge.jedis;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
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
        this.thisId = UChat.get().getConfig().root().jedis.server_id.replace("$", "");
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

            UChat.get().getLogger().info("REDIS conected.");
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
                UChat.get().getLogger().warning("REDIS not connected! Try again with /chat reload, or check the status of your Redis server.");
                pool.destroy();
                pool = null;
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public void sendTellMessage(CommandSource sender, String tellReceiver, Text msg) {
        Builder text = Text.builder();
        text.append(UCUtil.toText(this.thisId));

        //send spy
        if (!sender.hasPermission("uchat.chat-spy.bypass")) {
            for (Player receiver : Sponge.getServer().getOnlinePlayers()) {
                if (!receiver.getName().equals(tellReceiver) && !receiver.equals(sender) &&
                        UChat.get().isSpy.contains(receiver.getName()) && UChat.get().getPerms().hasSpyPerm(receiver, "private")) {
                    String spyformat = UChat.get().getConfig().root().general.spy_format;

                    spyformat = spyformat.replace("{output}", UCUtil.stripColor('&', UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell")).toPlain()));
                    receiver.sendMessage(UCUtil.toText(spyformat));
                }
            }
        }

        text.append(UCMessages.sendMessage(sender, tellReceiver, msg, new UCChannel("tell")));
        tellPlayers.put(tellReceiver, sender.getName());

        if (Arrays.asList(channels).contains("tellsend")) {
            Sponge.getScheduler().createAsyncExecutor(UChat.get()).execute(() -> {
                Jedis jedis = pool.getResource();
                try {
                    //string 0 1 2
                    jedis.publish("tellsend", this.thisId + "$" + tellReceiver + "$" + TextSerializers.JSON.serialize(text.build()));
                } catch (JedisConnectionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void sendRawMessage(Text value) {

        if (Arrays.asList(channels).contains("generic")) {
            Sponge.getScheduler().createAsyncExecutor(UChat.get()).execute(() -> {
                Jedis jedis = pool.getResource();
                try {
                    //string 0 1
                    jedis.publish("generic", this.thisId + "$" + TextSerializers.JSON.serialize(value));
                } catch (JedisConnectionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void sendMessage(String channel, Text value) {

        if (Arrays.asList(channels).contains(channel)) {
            Sponge.getScheduler().createAsyncExecutor(UChat.get().instance()).execute(() -> {
                //connectPool();
                Jedis jedis = pool.getResource();
                try {
                    //string 0 1
                    jedis.publish(channel, this.thisId + "$" + TextSerializers.JSON.serialize(value));
                } catch (JedisConnectionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void closePool() {
        UChat.get().getLogger().info("Closing REDIS...");
        if (psl != null) psl.poison();
        if (pool != null) pool.destroy();
        UChat.get().getLogger().info("REDIS closed.");
    }

    private class PubSubListener implements Runnable {

        @Override
        public void run() {
            try {
                Jedis rsc = pool.getResource();
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