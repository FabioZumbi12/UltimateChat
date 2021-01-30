package br.net.fabiozumbi12.UltimateChat.Bukkit.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UChatColor {

    public static final String HEX_PATTERN = "&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})";

    public static String stripColor(String text) {
        text = ChatColor.stripColor(text);
        text = text.replaceAll(HEX_PATTERN, "");
        return text;
    }

    public static String translateAlternateColorCodes(String text) {
        Matcher matcher = Pattern.compile(HEX_PATTERN).matcher(text);
        while (matcher.find()) {
            String toReplace = matcher.group(0);
            String find = matcher.group(1);
            text = text.replace(toReplace, "§#"+find);
        }
        return ChatColor.translateAlternateColorCodes('&',text);
    }
}
