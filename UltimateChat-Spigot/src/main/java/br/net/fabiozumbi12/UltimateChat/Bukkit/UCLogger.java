package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.ChatColor;

public class UCLogger {

    private final UChat uchat;
    private long start = 0;
    public UCLogger(UChat uChat) {
        this.uchat = uChat;
    }

    public void logClear(String s) {
        uchat.getServer().getConsoleSender().sendMessage(s);
    }

    public void sucess(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&a&l" + s + "&r]"));
    }

    public void info(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [" + s + "&r]"));
    }

    public void warning(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&6" + s + "&r]"));
    }

    public void severe(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&c&l" + s + "&r]"));
    }

    public void log(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [" + s + "&r]"));
    }

    public void debug(String s) {
        if (UChat.get().getUCConfig() != null && UChat.get().getUCConfig().getBoolean("debug.messages")) {
            uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&b" + s + "&r]"));
        }
    }

    public void timings(timingType type, String message) {
        if (UChat.get().getUCConfig() != null && UChat.get().getUCConfig().getBoolean("debug.timings")) {
            switch (type) {
                case START:
                    long diff = 0;
                    if (System.currentTimeMillis() - start > 5000) start = 0;
                    if (start != 0) {
                        diff = System.currentTimeMillis() - start;
                    }
                    start = System.currentTimeMillis();
                    uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3UC Timings - " + type + ": " + diff + "ms (" + message + "&3)&r"));
                    break;
                case END:
                    uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3UC Timings - " + type + ": " + (System.currentTimeMillis() - start) + "ms (" + message + "&3)&r"));
                    break;
                default:
                    break;
            }
        }
    }

    public enum timingType {
        START, END
    }
}
