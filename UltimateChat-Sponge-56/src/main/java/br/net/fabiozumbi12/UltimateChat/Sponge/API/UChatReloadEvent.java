package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * <p>This event tells to event handlers when UChat reloads to let listeners reload your channels or for other UChat changes.</p>
 */
public class UChatReloadEvent extends AbstractEvent implements Event {

    public UChatReloadEvent() {
    }

    @Override
    public Cause getCause() {
        return UChat.get().getVHelper().getCause(UChat.get().instance());
    }
}
