package br.com.devpaulo.legendchat.api.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Classe apenas para compatibilidade com o LegendChat para setar tags.
 * Só o método 'setTagValue' funciona nessa classe.
 *
 * @author Fabio
 */
public class ChatMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final HashMap<String, String> tags;
    private final CommandSender sender;
    private boolean cancel = false;
    private String message;

    public ChatMessageEvent(CommandSender sender, HashMap<String, String> tags, String message) {
        this.sender = sender;
        this.tags = tags;
        this.message = message;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Player getSender() {
        if (this.sender instanceof Player) {
            return (Player) this.sender;
        }
        return null;
    }

    public boolean setTagValue(String tagname, String value) {
        addTag(tagname, value);
        return true;
    }

    public String getTagValue(String tag) {
        return tags.get(tag);
    }

    public void addTag(String tagname, String value) {
        tags.put(tagname, value);
    }

    public HashMap<String, String> getTagMap() {
        return tags;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags.keySet());
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
