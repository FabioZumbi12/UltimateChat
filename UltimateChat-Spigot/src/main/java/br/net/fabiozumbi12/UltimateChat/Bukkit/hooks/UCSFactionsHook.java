package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;

public class UCSFactionsHook implements UCFactionsHookInterface {
    @Override
    public String formatFac(String text, Player sender, Object receiver) {
        FPlayer fp = FPlayers.getInstance().getByPlayer(sender);
        if (!fp.getFaction().isWilderness()) {
            Faction fac = fp.getFaction();
            text = text
                    .replace("{fac-id}", fac.getId())
                    .replace("{fac-name}", fac.getTag())
                    .replace("{fac-description}", fac.getDescription());
           // Saber doesn't have MOTD support
            if (receiver instanceof Player) {
                FPlayer recmp = FPlayers.getInstance().getByPlayer((Player) receiver);
                text = text
                        .replace("{fac-relation-name}", fac.getTag(recmp))
                        .replace("{fac-relation-color}", fac.getColorTo(recmp).toString());
            }
        }
        return text;
    }
}
