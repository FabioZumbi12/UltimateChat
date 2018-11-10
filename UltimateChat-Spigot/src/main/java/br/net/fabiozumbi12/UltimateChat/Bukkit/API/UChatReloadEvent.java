package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>This event tells to event handlers when UChat reloads to let listeners reload your channels or for other UChat changes.</p>
 */
public class UChatReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public UChatReloadEvent() {
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
