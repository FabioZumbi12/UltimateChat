package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

public interface UCVHelper {
	public Cause getCause(CommandSource src);

    public Cause getCause(PluginContainer instance);
	
	
}
