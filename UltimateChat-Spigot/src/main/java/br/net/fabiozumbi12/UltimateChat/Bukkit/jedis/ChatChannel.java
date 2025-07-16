/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 16/07/2025 18:07
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.UltimateChat.Bukkit.jedis;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;

public class ChatChannel extends JedisPubSub {
    private final String[] channels;
    private final String thisId;

    public ChatChannel(String[] channels) {
        this.channels = channels;
        this.thisId = UChat.get().getUCConfig().getString("jedis.server-id").replace("$", "");
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!UChat.get().getUCConfig().getBoolean("debug.messages") && message.split("\\$")[0].equals(this.thisId))
            return;

        if (Arrays.asList(channels).contains(channel)) {
            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                if (channel.equals("tellresponse")) {
                    String[] tellresp = message.split("@");
                    if (tellresp[0].equals(thisId)) return;
                    if (UChat.get().getJedis().tellPlayers.containsKey(tellresp[1])) {
                        Player sender = Bukkit.getPlayer(UChat.get().getJedis().tellPlayers.get(tellresp[1]));
                        if (sender != null && sender.isOnline()) {
                            if (tellresp[2].equals("false")) {
                                UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("listener.invalidplayer"));
                            } else {
                                UCUtil.sendTellRaw(sender, Bukkit.getConsoleSender(), tellresp[3]);
                            }
                        }
                    }
                    return;
                }

                if (channel.equals("tellsend")) {
                    String[] msgc = message.split("\\$");

                    String id = msgc[0];
                    String tellrec = msgc[1];
                    String messagef = msgc[2];

                    Player play = Bukkit.getPlayer(tellrec);
                    if (play == null) {
                        UChat.get().getJedis().getPool().getResource().publish("tellresponse", thisId + "@" + tellrec + "@false");
                        return;
                    } else {
                        UChat.get().getJedis().getPool().getResource().publish("tellresponse", thisId + "@" + tellrec + "@true@" + messagef.replace("@", ""));
                    }
                    UChat.get().getJedis().tellPlayers.remove(tellrec);
                    Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Private message from server " + id + " to player " + tellrec));

                    //send
                    UCUtil.sendTellRaw(play, Bukkit.getConsoleSender(), messagef);
                    return;
                }

                if (!channel.equals("generic")) {
                    String[] msgc = message.split("\\$");

                    String id = msgc[0];
                    String messagef = msgc[1];

                    UCChannel ch = UChat.get().getChannel(channel);
                    if (ch == null || !ch.useJedis()) return;

                    if (ch.getDistance() == 0) {
                        if (ch.neeFocus()) {
                            for (String receiver : ch.getMembers()) {
                                if (Bukkit.getPlayer(receiver) != null) {
                                    UCUtil.sendTellRaw(Bukkit.getPlayer(receiver), Bukkit.getConsoleSender(), messagef);
                                }
                            }
                        } else {
                            for (Player receiver : Bukkit.getServer().getOnlinePlayers()) {
                                if (UCPerms.channelReadPerm(receiver, ch)) {
                                    UCUtil.sendTellRaw(receiver, Bukkit.getConsoleSender(), messagef);
                                }
                            }
                        }
                    }
                    Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Redis message to channel " + ch.getName() + " from: " + id));
                } else {
                    String[] msgc = message.split("\\$");

                    String id = msgc[0];
                    String messagef = msgc[1];
                    for (Player receiver : Bukkit.getServer().getOnlinePlayers()) {
                        UCUtil.sendTellRaw(receiver, Bukkit.getConsoleSender(), messagef);
                    }
                    Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Raw Message from: " + id));
                }
            });
        }
    }
}