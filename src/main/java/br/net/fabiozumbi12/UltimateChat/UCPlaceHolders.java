package br.net.fabiozumbi12.UltimateChat;

import me.clip.placeholderapi.external.EZPlaceholderHook;

import org.bukkit.entity.Player;

public class UCPlaceHolders extends EZPlaceholderHook {

	public UCPlaceHolders(UChat plugin) {
		super(plugin, "uchat");
	}

	@Override
	public String onPlaceholderRequest(Player p, String arg) {
		if (arg.equals("player_channel_name")){
			return UChat.config.getChannel(UChat.pChannels.get(p.getName())).getName();
		}
		if (arg.equals("player_channel_alias")){
			return UChat.config.getChannel(UChat.pChannels.get(p.getName())).getAlias();
		}
		if (arg.equals("player_channel_color")){
			return UChat.config.getChannel(UChat.pChannels.get(p.getName())).getColor();
		}
		if (arg.equals("player_tell_with") && UChat.tellPlayers.containsKey(p.getName())){
			return UChat.tellPlayers.get(p.getName());
		}
		if (arg.equals("player_ignoring") && UChat.ignoringPlayer.containsKey(p.getName())){
			return UChat.ignoringPlayer.get(p.getName()).toArray().toString();
		}
		if (arg.equals("default_channel")){
			return UChat.config.getDefChannel().getName();
		}		
		return null;
	}
}
