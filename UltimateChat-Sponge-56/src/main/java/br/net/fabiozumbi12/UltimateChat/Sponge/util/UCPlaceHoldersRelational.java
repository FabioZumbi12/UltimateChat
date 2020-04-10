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

package br.net.fabiozumbi12.UltimateChat.Sponge.util;

import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;

public class UCPlaceHoldersRelational {

    public UCPlaceHoldersRelational(UChat plugin) {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);

        service.loadAll(this, plugin).forEach(builder -> {
            if (builder.getId().startsWith("uchat-")) {
                builder.author("FabioZumbi12");
                builder.version(plugin.instance().getVersion().get());
                try {
                    builder.buildAndRegister();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Placeholder(id = "uchat-channelname")
    public String channelName(@Source CommandSource p) {
        return UChat.get().getPlayerChannel(p).getName();
    }

    @Placeholder(id = "uchat-channelalias")
    public String channelAlias(@Source CommandSource p) {
        return UChat.get().getPlayerChannel(p).getAlias();
    }

    @Placeholder(id = "uchat-channelcolor")
    public String channelColor(@Source CommandSource p) {
        return UChat.get().getPlayerChannel(p).getColor();
    }

    @Placeholder(id = "uchat-tellwith")
    public String tellWith(@Source CommandSource p) {
        if (UChat.get().tellPlayers.containsKey(p.getName())) {
            return UChat.get().tellPlayers.get(p.getName());
        }
        return "--";
    }

    @Placeholder(id = "uchat-ignoring")
    public String ignoringPlayer(@Source CommandSource p) {
        if (UChat.get().ignoringPlayer.containsKey(p.getName())) {
            return Arrays.toString(UChat.get().ignoringPlayer.get(p.getName()).toArray());
        }
        return "--";
    }

    @Placeholder(id = "uchat-defaultchannel")
    public String defaultChannel(@Source CommandSource p) {
        return UChat.get().getDefChannel(p instanceof Player ? ((Player) p).getWorld().getName() : null).getName();
    }
}
