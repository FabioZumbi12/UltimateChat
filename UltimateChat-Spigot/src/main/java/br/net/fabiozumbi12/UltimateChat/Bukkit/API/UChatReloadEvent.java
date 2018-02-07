package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UChatReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public UChatReloadEvent(){}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
