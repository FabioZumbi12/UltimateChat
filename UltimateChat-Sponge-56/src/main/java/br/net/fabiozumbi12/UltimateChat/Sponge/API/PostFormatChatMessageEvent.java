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
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;

import java.util.HashMap;

/**
 * This event listen all text to be sent to players formated and colored, ready to send to chat.<p>
 * This event includes console. This event will listen to private messages too.<p>
 * <p>
 * Cancelling this event will not cancel SendMessageEvent.Chat but only will replace the channel.
 *
 * @author FabioZumbi12
 */
public class PostFormatChatMessageEvent extends AbstractEvent implements Cancellable, Event {
    private final CommandSource sender;
    private final UCChannel channel;
    private final HashMap<CommandSource, Text> receivers;
    private boolean cancelled;

    public PostFormatChatMessageEvent(CommandSource sender, HashMap<CommandSource, Text> receivers, UCChannel channel) {
        this.sender = sender;
        this.channel = channel;
        this.receivers = receivers;
    }

    public UCChannel getChannel() {
        return this.channel;
    }

    public CommandSource getSender() {
        return this.sender;
    }

    /**
     * Get the message of a receiver, or {@code null} if the receiver is not on list.
     *
     * @param receiver {@CommandSource}
     * @return {@code Text} or null if no receivers on this map.
     */
    public Text getReceiverMessage(CommandSource receiver) {
        return this.receivers.getOrDefault(receiver, null);
    }

    /**
     * Change or add a message and a receiver to the receivers list.
     *
     * @param receiver {@code CommandSource}
     * @param message  {@code Text}
     */
    public void setReceiverMessage(CommandSource receiver, Text message) {
        this.receivers.put(receiver, message);
    }

    @Override
    public Cause getCause() {
        return UChat.get().getVHelper().getCause(this.sender);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
