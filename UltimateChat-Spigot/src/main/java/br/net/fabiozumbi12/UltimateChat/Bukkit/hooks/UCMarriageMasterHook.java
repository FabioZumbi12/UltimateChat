package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageMasterPlugin;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class UCMarriageMasterHook {
    private MarriageMasterPlugin marriageMasterPlugin;
    
    public UCMarriageMasterHook(Plugin plugin){
        marriageMasterPlugin = (MarriageMasterPlugin)plugin;
    }
    
    public String parseMarryTags(String text, OfflinePlayer sender){
        MarriagePlayer mPlayer = marriageMasterPlugin.getPlayerData(sender);
        if (mPlayer.isMarried() && mPlayer.getPartner() != null) {
            text = text.replace("{marry-partner}", mPlayer.getPartner().getName())
                    .replace("{marry-prefix}", marriageMasterPlugin.getPrefixSuffixFormatter().formatPrefix(mPlayer.getMarriageData(mPlayer.getPartner()), mPlayer.getPartner()))
                    .replace("{marry-suffix}", marriageMasterPlugin.getPrefixSuffixFormatter().formatSuffix(mPlayer.getMarriageData(mPlayer.getPartner()), mPlayer.getPartner()));
        }
        return text;
    }
}
