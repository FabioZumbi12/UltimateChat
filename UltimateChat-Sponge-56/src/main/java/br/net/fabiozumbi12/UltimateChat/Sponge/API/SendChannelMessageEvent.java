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
 * This event listen to all chat messages sent by players before get formated and separated by player and channel.
 *
 * @author FabioZumbi12
 */
public class SendChannelMessageEvent extends AbstractEvent implements Cancellable, Event {

    private final CommandSource sender;
    private final boolean cancelIncoming;
    private boolean cancelled;
    private Text msg;
    private UCChannel channel;
    private String[] defBuilder;
    private String[] defFormat;
    private HashMap<String, String> registeredTags;

    public SendChannelMessageEvent(HashMap<String, String> registeredReplacers, String[] defFormat, CommandSource sender, UCChannel channel, Text msg, boolean cancelIncoming) {
        this.sender = sender;
        this.msg = msg;
        this.channel = channel;
        this.defBuilder = UChat.get().getConfig().getDefBuilder();
        this.defFormat = defFormat;
        this.registeredTags = registeredReplacers;
        this.cancelIncoming = cancelIncoming;
    }

    public CommandSource getSender() {
        return this.sender;
    }

    /**
     * Removes a custom registered tag {@code replacer}.
     *
     * @param tagname - Tag to remove.
     * @return {@code true} if removed or {@code false} if dont contains the replacer.
     */
    public boolean removeTag(String tagname) {
        if (this.registeredTags.keySet().contains(tagname)) {
            this.registeredTags.remove(tagname);
            return true;
        }
        return false;
    }

    /**
     * Register a tag and value. Add your chat tags here.
     *
     * @param tagname {@code String} - The tag name.
     * @param value   {@code String} - Result to show on chat.
     * @return {@code true} if added or {@code false} if already contains the tag.
     */
    public boolean addTag(String tagname, String value) {
        if (!this.registeredTags.keySet().contains(tagname)) {
            this.registeredTags.put(tagname, value);
            return true;
        }
        return false;
    }

    public void setTags(HashMap<String, String> tags) {
        this.registeredTags = tags;
    }

    /**
     * Get all registered tags.
     *
     * @return {@code HashMap<String,String>} with all registered replacers.
     */
    public HashMap<String, String> getResgisteredTags() {
        return this.registeredTags;
    }

    /**
     * Get an array of default vanilla tags set by other plugins as array.
     * <p>
     * [0] = Header, [1] = Body, [2] = Footer
     * <p>
     *
     * @return String[] With default tags.
     */
    public String[] getDefFormat() {
        return this.defFormat;
    }

    /**
     * Set the default tags array to send to chat.
     * <p>
     * [0] = Header, [1] = Body, [2] = Footer
     * <p>
     * Note: This will not change the tags added by uChat, only default tags and custom tags by other plugins.
     *
     * @param defFormat - The array as String[].
     */
    public void setDefFormat(String[] defFormat) {
        this.defFormat = defFormat;
    }

    /**
     * This is the array of tag names in order what will be sent do chat. Add tag names here only you have added one on vonfiguration.
     *
     * @param builder - tag name.
     */
    public void setDefBuilder(String[] builder) {
        this.defBuilder = builder;
    }

    /**
     * Get an array of the tag names in order to show on chat.
     *
     * @return String[] - Ordered array with all tag names.
     */
    public String[] getDefaultBuilder() {
        return defBuilder;
    }

    /**
     * Get the actual channel will be send the message.
     *
     * @return UCChannel to send the message or {@code null} if is a private message.
     */
    public UCChannel getChannel() {
        return this.channel;
    }

    /**
     * Sets new channel to send the message
     *
     * @param newCh - New UCChannel component to send the message.
     */
    public void setChannel(String newCh) {
        this.channel = UChat.get().getChannel(newCh);
    }

    /**
     * Change the message to send to chat.
     *
     * @param newMsg - Text with new message.
     */
    public void setMessage(Text newMsg) {
        this.msg = newMsg;
    }

    /**
     * Get the message that will be send to chat.
     *
     * @return Text with the message.
     */
    public Text getMessage() {
        return this.msg;
    }

    /**
     * Change the message to send to chat.
     *
     * @param newMsg - String with new message.
     */
    @Deprecated
    public void setMessage(String newMsg) {
        this.msg = Text.of(newMsg);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return UChat.get().getVHelper().getCause(this.sender);
    }

    public boolean getCancelIncomingChat() {
        return cancelIncoming;
    }


}
