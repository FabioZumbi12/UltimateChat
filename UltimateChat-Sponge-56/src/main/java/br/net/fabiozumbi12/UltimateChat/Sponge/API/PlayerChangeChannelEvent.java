package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * <p>Event fired when a player change from channel A to channel B.</p>
 * <i>Channel A may be null if the channel is deleted by in-game delete commmand.</i>
 */
public class PlayerChangeChannelEvent extends AbstractEvent implements Event, Cancellable {
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
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return UChat.get().getVHelper().getCause(this.player);
    }

}
