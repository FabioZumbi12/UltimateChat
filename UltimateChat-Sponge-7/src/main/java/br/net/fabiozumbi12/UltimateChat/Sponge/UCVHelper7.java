package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.plugin.PluginContainer;

public class UCVHelper7 implements UCVHelper {

	@Override
	public Cause getCause(CommandSource src) {
		return Cause.of(EventContext.empty(), src);
	}

	@Override
	public Cause getCause(PluginContainer instance) {
		return Cause.of(EventContext.empty(), instance);
	}
}
