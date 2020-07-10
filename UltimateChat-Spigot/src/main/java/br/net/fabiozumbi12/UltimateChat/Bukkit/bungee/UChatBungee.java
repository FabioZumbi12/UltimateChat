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

package br.net.fabiozumbi12.UltimateChat.Bukkit.bungee;

import br.net.fabiozumbi12.UltimateChat.Bukkit.*;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCUtil;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class UChatBungee implements PluginMessageListener, Listener {

    public static void sendBungee(UCChannel ch, UltimateFancy text) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(UChat.get().getUCConfig().getString("bungee.server-id"));
        out.writeUTF(ch.getAlias());
        out.writeUTF(text.toString());

        Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        try {
            p.sendPluginMessage(UChat.get(), "bungee:uchat", out.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("bungee:uchat")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        String id = "";
        String ch = "";
        String json = "";
        try {
            id = in.readUTF();
            ch = in.readUTF();
            json = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UCChannel chan = UChat.get().getChannel(ch);
        if (chan == null || !chan.isBungee()) {
            return;
        }

        if (chan.getDistance() == 0) {
            if (chan.neeFocus()) {
                for (String receiver : chan.getMembers()) {
                    if (Bukkit.getPlayer(receiver) != null) {
                        UCUtil.sendTellRaw(Bukkit.getPlayer(receiver), Bukkit.getConsoleSender(), json);
                    }
                }
            } else {
                for (Player receiver : Bukkit.getServer().getOnlinePlayers()) {
                    if (UCPerms.channelReadPerm(receiver, chan)) {
                        UCUtil.sendTellRaw(receiver, Bukkit.getConsoleSender(), json);
                    }
                }
            }
        }
        Bukkit.getConsoleSender().sendMessage(UCUtil.colorize("&7Bungee message to channel " + chan.getName() + " from: " + id));
    }
}
