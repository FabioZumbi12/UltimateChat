package br.net.fabiozumbi12.UltimateChat.Bukkit;

import me.clip.placeholderapi.external.EZPlaceholderHook;

import org.bukkit.entity.Player;

public class UCPlaceHolders extends EZPlaceholderHook {

	public UCPlaceHolders(UChat plugin) {
		super(plugin, "uchat");
	}

	@Override
	public String onPlaceholderRequest(Player p, String arg) {
		String text = "--";
		if (arg.equals("player_channel_name")){
			text = UChat.get().getUCConfig().getPlayerChannel(p).getName();
		}
		if (arg.equals("player_channel_alias")){
			text = UChat.get().getUCConfig().getPlayerChannel(p).getAlias();
		}
		if (arg.equals("player_channel_color")){
			text = UChat.get().getUCConfig().getPlayerChannel(p).getColor();
		}
		if (arg.equals("player_tell_with") && UChat.get().tellPlayers.containsKey(p.getName())){
			text = UChat.get().tellPlayers.get(p.getName());
		}
		if (arg.equals("player_ignoring") && UChat.get().ignoringPlayer.containsKey(p.getName())){
			text = UChat.get().ignoringPlayer.get(p.getName()).toArray().toString();
		}
		if (arg.equals("default_channel")){
			text = UChat.get().getUCConfig().getDefChannel().getName();
		}		
		return text;
	}
}
