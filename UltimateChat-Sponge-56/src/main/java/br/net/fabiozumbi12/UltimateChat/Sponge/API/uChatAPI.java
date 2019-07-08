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

package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCChatProtection;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.TagsCategory;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.util.*;

public class uChatAPI {

    public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages, String permission, List<String> shoinworlds, List<String> hideinworlds, String clickUrl) {
        TagsCategory tagsCat = new TagsCategory(format, clickCmd, hoverMessages, permission, shoinworlds, hideinworlds, clickUrl);
        UChat.get().getConfig().root().tags.put(tagName, tagsCat);
        return true;
    }

    public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages) {
        return registerNewTag(tagName, format, clickCmd, hoverMessages, null, null, null, null);
    }

    public boolean registerNewChannel(UCChannel channel) throws IOException {
        UChat.get().getConfig().addChannel(channel);
        UChat.get().getCmds().registerChannelAliases();
        UChat.get().reload();
        return true;
    }

    public boolean registerNewChannel(Map<String, Object> properties) throws IOException {
        UCChannel ch = new UCChannel(properties);
        return registerNewChannel(ch);
    }

    @Deprecated
    public boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance, String color, String tagBuilder, boolean needFocus, boolean receiverMsg, double cost, String ddmode, String ddmcformat, String mcddformat, String ddhover, boolean ddallowcmds, boolean bungee) throws IOException {
        if (UChat.get().getChannel(chName) != null) {
            return false;
        }
        if (tagBuilder == null || tagBuilder.equals("")) {
            tagBuilder = UChat.get().getConfig().root().general.default_tag_builder;
        }
        UCChannel ch = new UCChannel(chName, chAlias, crossWorlds, distance, color, tagBuilder, needFocus, receiverMsg, cost, bungee, true, false, false, "player", "", new ArrayList<>(), "", ddmode, ddmcformat, mcddformat, ddhover, ddallowcmds, true);
        UChat.get().getConfig().addChannel(ch);
        UChat.get().reload();
        return true;
    }

    public UCChannel getChannel(String chName) {
        return UChat.get().getChannel(chName);
    }

    public UCChannel getPlayerChannel(Player player) {
        return UChat.get().getPlayerChannel(player);
    }

    public Collection<UCChannel> getChannels() {
        return UChat.get().getChannels().values();
    }

    /**
     * Filter your message strings by using uchat protections with this method.
     *
     * @param receiver The receiver
     * @param message  String message
     * @param channel  Receiver channel
     * @return Filtered message as string.
     */
    public String filterChatMessage(Player receiver, String message, UCChannel channel) {
        return UCChatProtection.filterChatMessage(receiver, message, channel);
    }

    /**
     * Get formated tag format from config with placeholders already parsed.
     *
     * @param tagname  Tag name from {@code tags} config section.
     * @param sender   The player to be the sender/owner of parsed tag.
     * @param receiver The player as receiver of tag. Use {@link Optional}.empty() to do not use a receiver.
     * @return Formatted tag or {@code null} if the tag is not on config.
     */
    public String getTagFormat(String tagname, Player sender, Optional<Player> receiver) {
        if (UChat.get().getConfig().root().tags.containsKey(tagname)) {
            String format = UChat.get().getConfig().root().tags.get(tagname).format;
            return UCMessages.formatTags(tagname, format, sender, receiver.isPresent() ? receiver.get() : "", "", UChat.get().getPlayerChannel(sender));
        }
        return null;
    }
}
