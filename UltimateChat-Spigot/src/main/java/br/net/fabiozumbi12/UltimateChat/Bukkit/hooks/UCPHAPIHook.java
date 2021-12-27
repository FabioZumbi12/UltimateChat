package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class UCPHAPIHook {

    public String setRelationalPlaceholders(Player sender, Player receiver, String text){
        return PlaceholderAPI.setRelationalPlaceholders(sender, receiver, text);
    }

    public String setPlaceholders(Player sender, String text){
        return PlaceholderAPI.setPlaceholders(sender, text);
    }
}
