package br.net.fabiozumbi12.UltimateChat.Bukkit;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class UCPlaceHolders extends PlaceholderExpansion {

    private UChat plugin;
	public UCPlaceHolders(UChat plugin) {
	    this.plugin = plugin;
	}

	@Override
	public String onPlaceholderRequest(Player p, String arg) {
		String text = "--";
		if (arg.equals("player_channel_name")){
			text = UChat.get().getPlayerChannel(p).getName();
		}
		if (arg.equals("player_channel_alias")){
			text = UChat.get().getPlayerChannel(p).getAlias();
		}
		if (arg.equals("player_channel_color")){
			text = UChat.get().getPlayerChannel(p).getColor();
		}
		if (arg.equals("player_tell_with") && UChat.get().tellPlayers.containsKey(p.getName())){
			text = UChat.get().tellPlayers.get(p.getName());
		}
		if (arg.equals("player_ignoring") && UChat.get().ignoringPlayer.containsKey(p.getName())){
			text = Arrays.toString(UChat.get().ignoringPlayer.get(p.getName()).toArray());
		}
		if (arg.equals("default_channel")){
			text = UChat.get().getDefChannel().getName();
		}
        if (arg.startsWith("placeholder_")){
            String ph = arg.replace("placeholder_","");
            text = UCMessages.formatTags("", "{"+ph+"}", p, "", "", UChat.get().getPlayerChannel(p));
        }
		if (arg.startsWith("tag_")){
			String tag = arg.replace("tag_","");
			if (UChat.get().getUCConfig().getString("tags."+tag+".format") != null){
				String format = UChat.get().getUCConfig().getString("tags."+tag+".format");
				text = UCMessages.formatTags(tag, format, p, "", "", UChat.get().getPlayerChannel(p));
			}
		}
		return text;
	}

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "uchat";
    }

    @Override
    public String getPlugin() {
        return null;
    }

    @Override
    public String getAuthor() {
        return "FabioZumbi12";
    }

    @Override
    public String getVersion() {
        return this.plugin.getPDF().getVersion();
    }
}
