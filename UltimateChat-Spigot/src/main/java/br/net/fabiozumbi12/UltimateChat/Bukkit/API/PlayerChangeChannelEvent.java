package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerChangeChannelEvent extends Event  implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private UCChannel channelTo;
    private UCChannel channelFrom;
    private boolean isCancelled;

    public PlayerChangeChannelEvent(Player p, UCChannel from, UCChannel to){
        this.player = p;
        this.channelFrom = from;
        this.channelTo = to;
    }

    public Player getPlayer(){
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

    public static HandlerList getHandlerList() {
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
