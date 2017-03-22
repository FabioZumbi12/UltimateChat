package br.net.fabiozumbi12.UltimateChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class UCLogger{
	 
	public void logClear(String s) {
    	Bukkit.getConsoleSender().sendMessage("UltimateChat: ["+s+"]");
    }
	
	public void sucess(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: ["+s+"]"));
    }
    
    public void warning(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: ["+s+"]"));
    }
    
    public void debug(String s) {
        if (UChat.config != null && UChat.config.getBool("debug-messages")) {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&b"+s+"&r]"));
        }  
    }
}
