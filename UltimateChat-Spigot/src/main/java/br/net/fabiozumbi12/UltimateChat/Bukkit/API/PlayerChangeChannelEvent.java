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

package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>Event fired when a player change from channel A to channel B.</p>
 * <i>Channel A may be null if the channel is deleted by in-game delete commmand.</i>
 *
 * @author FabioZumbi12
 */
public class PlayerChangeChannelEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final UCChannel channelTo;
    private final UCChannel channelFrom;
    private boolean isCancelled;

    public PlayerChangeChannelEvent(Player p, UCChannel from, UCChannel to) {
        super(true);
        this.player = p;
        this.channelFrom = from;
        this.channelTo = to;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public UCChannel getChannelFrom() {
        return this.channelFrom;
    }

    public UCChannel getChannelTo() {
        return this.channelTo;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
