package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.command.CommandSender;

public interface UCDInterface {
	
	public void sendTellToDiscord(String text);
	
	public void sendRawToDiscord(String text);
	
	public void sendToDiscord(CommandSender sender, String text, UCChannel ch);
	
	public void updateGame(String text);
	
	public void shutdown();
}
