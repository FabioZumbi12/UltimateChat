package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.command.CommandSender;

public interface UCDInterface {
	boolean JDAAvailable();

	void sendTellToDiscord(String text);
	
	void sendRawToDiscord(String text);
	
	void sendToDiscord(CommandSender sender, String text, UCChannel ch);
	
	void updateGame(String text);
	
	void sendCommandsToDiscord(String text);

	int getTaskId();
	
	void shutdown();
}
