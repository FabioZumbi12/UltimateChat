package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;

public interface UCDInterface {
	
	public void sendTellToDiscord(String text);
	
	public void sendRawToDiscord(String text);
	
	public void sendToDiscord(CommandSource sender, String text, UCChannel ch);
	
	public void updateGame(String text);
	
	public void shutdown();
}
