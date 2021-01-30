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

package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;

public class UCPlaceHolders extends PlaceholderExpansion {

    private final UChat plugin;

    public UCPlaceHolders(UChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer p, String arg) {
        String text = "--";
        if (arg.equals("player_channel_name")) {
            text = UChat.get().getPlayerChannel(p.getPlayer()).getName();
        }
        if (arg.equals("player_channel_alias")) {
            text = UChat.get().getPlayerChannel(p.getPlayer()).getAlias();
        }
        if (arg.equals("player_channel_color")) {
            text = UChat.get().getPlayerChannel(p.getPlayer()).getColor();
        }
        if (arg.equals("player_tell_with") && UChat.get().tellPlayers.containsKey(p.getName())) {
            text = UChat.get().tellPlayers.get(p.getName());
        }
        if (arg.equals("player_ignoring") && UChat.get().ignoringPlayer.containsKey(p.getName())) {
            text = Arrays.toString(UChat.get().ignoringPlayer.get(p.getName()).toArray());
        }
        if (arg.equals("default_channel")) {
            text = UChat.get().getDefChannel(p.getPlayer().getWorld().getName()).getName();
        }
        if (arg.startsWith("placeholder_")) {
            String ph = arg.replace("placeholder_", "");
            text = UCMessages.formatTags("", "{" + ph + "}", p, "", "", UChat.get().getPlayerChannel(p.getPlayer()));
        }
        if (arg.startsWith("tag_")) {
            String tag = arg.replace("tag_", "");
            if (UChat.get().getUCConfig().getString("tags." + tag + ".format") != null) {
                String format = UChat.get().getUCConfig().getString("tags." + tag + ".format");
                text = UCMessages.formatTags(tag, format, p, "", "", UChat.get().getPlayerChannel(p.getPlayer()));
            }
        }
        return text;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "uchat";
    }

    @Override
    public String getAuthor() {
        return "FabioZumbi12";
    }

    @Override
    public String getVersion() {
        return this.plugin.getPDF().getVersion();
    }
}
