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
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;

/**
 * This event listen all fancy messages to be sent to players formated and colored, ready to send to chat.<p>
 * This event includes console. This event will listen to private messages too.<p>
 *
 * @author FabioZumbi12
 */
public class PostFormatChatMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private final UCChannel channel;
    private final HashMap<CommandSender, UltimateFancy> receivers;
    private boolean cancelled;
    private String raw;

    public PostFormatChatMessageEvent(CommandSender sender, HashMap<CommandSender, UltimateFancy> receivers, UCChannel channel, String raw, boolean isAsync) {
        super(isAsync);
        this.sender = sender;
        this.channel = channel;
        this.receivers = receivers;
        this.raw = raw;
    }

    public PostFormatChatMessageEvent(CommandSender sender, HashMap<CommandSender, UltimateFancy> receivers, UCChannel channel, String raw) {
        super(true);
        this.sender = sender;
        this.channel = channel;
        this.receivers = receivers;
        this.raw = raw;
    }

    @Deprecated
    public PostFormatChatMessageEvent(CommandSender sender, HashMap<CommandSender, UltimateFancy> receivers, UCChannel channel) {
        super(true);
        this.sender = sender;
        this.channel = channel;
        this.receivers = receivers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UCChannel getChannel() {
        return this.channel;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public String getRawMessage() {
        return this.raw;
    }

    /**
     * Get the message of a receiver, or {@code null} if the receiver is not on list.
     *
     * @param receiver {@code @CommandSender}
     * @return {@code Text} or null if no receivers on this map.
     */
    public UltimateFancy getReceiverMessage(CommandSender receiver) {
        return this.receivers.getOrDefault(receiver, null);
    }

    /**
     * Change or add a message and a receiver to the receivers list.
     *
     * @param receiver {@code @CommandSender}
     * @param message  {@code Text}
     */
    public void setReceiverMessage(CommandSender receiver, UltimateFancy message) {
        this.receivers.put(receiver, message);
    }

    /**
     * Get all receivers with your messages. Change, add or remove a receiver in this map.
     *
     * @return {@code HashMap<CommandSender, UltimateFancy>}
     */
    public HashMap<CommandSender, UltimateFancy> getMessages() {
        return this.receivers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
