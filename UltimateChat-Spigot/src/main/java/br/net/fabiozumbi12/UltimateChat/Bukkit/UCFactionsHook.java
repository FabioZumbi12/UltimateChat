package br.net.fabiozumbi12.UltimateChat.Bukkit;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.entity.Player;

public class UCFactionsHook {
	public static String formatFac(String text, Player sender, Object receiver){
		MPlayer mp = MPlayer.get(sender.getUniqueId());
		if (!mp.getFaction().isNone()){
			Faction fac = mp.getFaction();
			text = text
					.replace("{fac-id}", fac.getId())
					.replace("{fac-name}", fac.getName());
			if (fac.hasMotd()){
				text = text
						.replace("{fac-motd}", fac.getMotd());		
			}	
			if (fac.hasDescription()){
				text = text
						.replace("{fac-description}", fac.getDescription());		
			}
			if (receiver instanceof Player){
				MPlayer recmp = MPlayer.get(((Player)receiver).getUniqueId());
				text = text
						.replace("{fac-relation-name}", fac.getName(recmp))
						.replace("{fac-relation-color}", fac.getColorTo(recmp).toString());
			}
		}
		return text;
	}
}
